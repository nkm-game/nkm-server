package com.tosware.nkm.actors

import akka.actor.{ActorLogging, Cancellable, Props}
import akka.persistence.{PersistentActor, RecoveryCompleted}
import com.tosware.nkm.actors.Game.GameId
import com.tosware.nkm.models.CommandResponse._
import com.tosware.nkm.models.{GameEventMapped, GameStateValidator}
import com.tosware.nkm.models.game.Ability.AbilityId
import com.tosware.nkm.models.game.NkmCharacter.CharacterId
import com.tosware.nkm.models.game.CharacterMetadata.CharacterMetadataId
import com.tosware.nkm.models.game.Player.PlayerId
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.blindpick.BlindPickPhase
import com.tosware.nkm.models.game.draftpick.DraftPickPhase
import com.tosware.nkm.models.game.hex.HexCoordinates
import com.tosware.nkm.services.NkmDataService

import scala.concurrent.duration._
import scala.util.Random

object Game {
  type GameId = String
  sealed trait Query

  case object GetState extends Query

  case object GetCurrentClock extends Query

  case class GetStateView(forPlayer: Option[PlayerId]) extends Query

  sealed trait Command

  case class StartGame(gameStartDependencies: GameStartDependencies) extends Command

  case class Pause(playerId: PlayerId) extends Command

  case class Surrender(playerId: PlayerId) extends Command

  case class BanCharacters(playerId: PlayerId, characterIds: Set[CharacterMetadataId]) extends Command

  case class PickCharacter(playerId: PlayerId, characterId: CharacterMetadataId) extends Command

  case class BlindPickCharacters(playerId: PlayerId, characterId: Set[CharacterMetadataId]) extends Command

  case class PlaceCharacters(playerId: PlayerId, coordinatesToCharacterIdMap: Map[HexCoordinates, CharacterId]) extends Command

  case class EndTurn(playerId: PlayerId) extends Command

  case class PassTurn(playerId: PlayerId, characterId: CharacterId) extends Command

  case class MoveCharacter(playerId: PlayerId, path: Seq[HexCoordinates], characterId: CharacterId) extends Command

  case class BasicAttackCharacter(playerId: PlayerId, attackingCharacterId: CharacterId, targetCharacterId: CharacterId) extends Command

  case class UseAbilityWithoutTarget(playerId: PlayerId, abilityId: AbilityId) extends Command

  case class UseAbilityOnCoordinates(playerId: PlayerId, abilityId: AbilityId, target: HexCoordinates, useData: UseData) extends Command

  case class UseAbilityOnCharacter(playerId: PlayerId, abilityId: AbilityId, target: CharacterId, useData: UseData) extends Command

  // sent only by self /////////
  //
  case class CharacterSelectTimeout(pickNumber: Int) extends Command

  case class CharacterPlacingTimeout() extends Command

  case class TurnTimeout(turnNumber: Int) extends Command
  //////////////////////////////

  sealed trait Event {
    val id: GameId
  }
  case class GamePaused(id: GameId) extends Event

  case class GameUnpaused(id: GameId) extends Event

  case class GameStarted(id: GameId, gameStartDependencies: GameStartDependencies) extends Event

  case class Surrendered(id: GameId, playerId: PlayerId) extends Event

  case class CharactersBanned(id: GameId, playerId: PlayerId, characterIds: Set[CharacterMetadataId]) extends Event

  case class CharacterPicked(id: GameId, playerId: PlayerId, characterId: CharacterMetadataId) extends Event

  case class PlacingCharactersStarted(id: GameId) extends Event

  case class CharactersPlaced(id: GameId, playerId: PlayerId, coordinatesToCharacterIdMap: Map[HexCoordinates, CharacterId]) extends Event

  case class CharactersBlindPicked(id: GameId, playerId: PlayerId, characterId: Set[CharacterMetadataId]) extends Event

  case class TurnEnded(id: GameId, playerId: PlayerId) extends Event

  case class TurnPassed(id: GameId, playerId: PlayerId, characterId: CharacterId) extends Event

  case class CharacterMoved(id: GameId, playerId: PlayerId, path: Seq[HexCoordinates], characterId: CharacterId) extends Event

  case class CharacterBasicAttacked(id: GameId, playerId: PlayerId, attackingCharacterId: CharacterId, targetCharacterId: CharacterId) extends Event

  case class AbilityUsedWithoutTarget(id: GameId, playerId: PlayerId, abilityId: AbilityId) extends Event

  case class AbilityUsedOnCoordinates(id: GameId, playerId: PlayerId, abilityId: AbilityId, target: HexCoordinates, useData: UseData) extends Event

  case class AbilityUsedOnCharacter(id: GameId, playerId: PlayerId, abilityId: AbilityId, target: CharacterId, useData: UseData) extends Event

  // CLOCK TIMEOUTS /////////////////////
  //
  case class BanningPhaseTimedOut(id: GameId) extends Event

  case class DraftPickTimedOut(id: GameId) extends Event

  case class BlindPickTimedOut(id: GameId) extends Event

  case class TimeAfterPickTimedOut(id: GameId) extends Event

  case class CharacterPlacingTimedOut(id: GameId) extends Event

  case class TurnTimedOut(id: GameId) extends Event
  //////////////////////////////


  def props(id: GameId)(implicit nkmDataService: NkmDataService): Props = Props(new Game(id))
}

class Game(id: GameId)(implicit nkmDataService: NkmDataService) extends PersistentActor with ActorLogging {
  import Game._
  import context.dispatcher

  override def persistenceId: String = s"game-$id"

  implicit val random: Random = new Random(id.hashCode)

  implicit var gameState: GameState = GameState.empty(id)

  var scheduledTimeout: Cancellable = Cancellable.alreadyCancelled

  def v(): GameStateValidator = GameStateValidator()(gameState)

  def updateGameState(newGameState: GameState): Unit = {
    val lastGameState = gameState
    gameState = newGameState

    val newGameEvents = newGameState.gameLog.events
      .drop(lastGameState.gameLog.events.size)
      .map(e => GameEventMapped(id, e, newGameState.hiddenEvents.find(_.eid == e.id)))

    newGameEvents.foreach(context.system.eventStream.publish)
  }

  def updateGameStateAndScheduleDefault(newGameState: GameState): Unit = {
    updateGameState(newGameState)
    scheduleDefault()
  }

  def persistAndPublishAll[A](events: Seq[A])(handler: A => Unit): Unit = {
    log.warning("EVENT " + events.toString)
    persistAll(events)(handler)
  }

  def persistAndPublish[A](event: A)(handler: A => Unit): Unit = {
    log.warning("EVENT " + event.toString)
    persist(event)(handler)
  }

  def scheduleDefault(): Unit = {
    if(Seq(GameStatus.NotStarted, GameStatus.Finished).contains(gameState.gameStatus))
      return

    val timeout = if(gameState.isSharedTime)
      gameState.clock.sharedTime.millis
    else
      gameState.currentPlayerTime.millis

    val timeoutToSchedule: Command = gameState.gameStatus match {
      case GameStatus.Finished | GameStatus.NotStarted => ???
      case GameStatus.CharacterPick | GameStatus.CharacterPicked =>
        CharacterSelectTimeout(gameState.timeoutNumber)
      case GameStatus.CharacterPlacing =>
        CharacterPlacingTimeout()
      case GameStatus.Running =>
        TurnTimeout(gameState.turn.number)
    }
    scheduledTimeout.cancel()
    scheduledTimeout = context.system.scheduler.scheduleOnce(timeout)(self ! timeoutToSchedule)
  }


  def handleCharacterSelectTimeout(pickNumber: Int): Unit = {
    def startPlacingCharactersAfterTimeout(): Unit =
      persistAndPublishAll(Seq(
        TimeAfterPickTimedOut(id),
        PlacingCharactersStarted(id),
      ))(_ => updateGameStateAndScheduleDefault(gameState.startPlacingCharacters()))

    def banningPhaseTimeout(): Unit =
      persistAndPublish(BanningPhaseTimedOut(id))(_ => updateGameStateAndScheduleDefault(gameState.finishBanningPhase()))

    def draftPickTimeout(): Unit =
      persistAndPublish(DraftPickTimedOut(id))(_ => updateGameStateAndScheduleDefault(gameState.draftPickTimeout()))

    def blindPickTimeout(): Unit =
      persistAndPublish(BlindPickTimedOut(id))(_ => updateGameStateAndScheduleDefault(gameState.blindPickTimeout()))

    gameState.pickType match {
      case PickType.AllRandom =>
        startPlacingCharactersAfterTimeout()
      case PickType.DraftPick =>
        val draftPickState = gameState.draftPickState.get
        if (draftPickState.pickNumber == pickNumber) {
          draftPickState.pickPhase match {
            case DraftPickPhase.Banning =>
              banningPhaseTimeout()
            case DraftPickPhase.Picking =>
              draftPickTimeout()
            case DraftPickPhase.Finished =>
              startPlacingCharactersAfterTimeout()
          }
        }
      case PickType.BlindPick =>
        val blindPickState = gameState.blindPickState.get
        if (blindPickState.pickNumber == pickNumber) {
          blindPickState.pickPhase match {
            case BlindPickPhase.Picking =>
              blindPickTimeout()
            case BlindPickPhase.Finished =>
              startPlacingCharactersAfterTimeout()
          }
        }
    }
  }

  def pauseGame(): Unit =
    persistAndPublish(GamePaused(id)) { _ =>
      scheduledTimeout.cancel()
      updateGameState(gameState.pause())
      sender() ! Success()
    }


  def unpauseGame(): Unit =
    persistAndPublish(GameUnpaused(id)) { _ =>
      updateGameStateAndScheduleDefault(gameState.unpause())
      sender() ! Success()
    }

  def validate(response: CommandResponse)(onSuccess: () => Unit): Unit = {
    response match {
      case failure @ Failure(_) => sender() ! failure
      case Success(_) =>
        onSuccess()
    }
  }

  def startGame(gameStartDependencies: GameStartDependencies): Unit = {
    val e = GameStarted(id, gameStartDependencies)
    persistAndPublish(e) { _ =>
      updateGameStateAndScheduleDefault(gameState.startGame(gameStartDependencies))
      sender() ! Success()
    }
  }


  def surrender(playerId: PlayerId): Unit = {
    val e = Surrendered(id, playerId)
    persistAndPublish(e) { _ =>
      updateGameStateAndScheduleDefault(gameState.surrender(playerId))
      sender() ! Success()
    }
  }

  def banCharacters(playerId: PlayerId, characterIds: Set[CharacterMetadataId]): Unit =
  {
    val e = CharactersBanned(id, playerId, characterIds)
    persistAndPublish(e) { _ =>
      updateGameState(gameState.ban(playerId, characterIds))
      if(!gameState.isDraftBanningPhase)
        scheduleDefault()
      sender() ! Success()
    }
  }

  def pickCharacter(playerId: PlayerId, characterId: CharacterMetadataId): Unit = {
    val e = CharacterPicked(id, playerId, characterId)
    persistAndPublish(e) { _ =>
      updateGameStateAndScheduleDefault(gameState.pick(playerId, characterId))
      sender() ! Success()
    }
  }

  def blindPickCharacters(playerId: PlayerId, characterIds: Set[CharacterMetadataId]): Unit = {
    val e = CharactersBlindPicked(id, playerId, characterIds)
    persistAndPublish(e) { _ =>
      updateGameState(gameState.blindPick(playerId, characterIds))
      sender() ! Success()
      if(gameState.characterPickFinished)
        scheduleDefault()
    }
  }


  def placeCharacters(playerId: PlayerId, coordinatesToCharacterIdMap: Map[HexCoordinates, CharacterId]): Unit =
  {
    val e = CharactersPlaced(id, playerId, coordinatesToCharacterIdMap)
    persistAndPublish(e) { _ =>
      updateGameState(gameState.placeCharacters(playerId, coordinatesToCharacterIdMap))
      sender() ! Success()
      if(gameState.placingCharactersFinished)
        scheduleDefault()
    }
  }


  def endTurn(playerId: PlayerId): Unit = {
    val e = TurnEnded(id, playerId)
    persistAndPublish(e) { _ =>
      updateGameStateAndScheduleDefault(gameState.endTurn())
      sender() ! Success()
    }
  }

  def passTurn(playerId: PlayerId, characterId: CharacterId): Unit = {
    val e = TurnPassed(id, playerId, characterId)
    persistAndPublish(e) { _ =>
      updateGameStateAndScheduleDefault(gameState.passTurn(characterId))
      sender() ! Success()
    }
  }

  def moveCharacter(playerId: PlayerId, path: Seq[HexCoordinates], characterId: CharacterId): Unit = {
    val e = CharacterMoved(id, playerId, path, characterId)
    persistAndPublish(e) { _ =>
      updateGameState(gameState.basicMoveCharacter(characterId, path))
      sender() ! Success()
    }
  }


  def basicAttackCharacter(playerId: PlayerId, attackingCharacterId: CharacterId, targetCharacterId: CharacterId): Unit = {
    val e = CharacterBasicAttacked(id, playerId, attackingCharacterId, targetCharacterId)
    persistAndPublish(e) { _ =>
      updateGameState(gameState.basicAttack(attackingCharacterId, targetCharacterId))
      sender() ! Success()
    }
  }

  def useAbilityWithoutTarget(playerId: PlayerId, abilityId: AbilityId): Unit = {
    val e = AbilityUsedWithoutTarget(id, playerId, abilityId)
    persistAndPublish(e) { _ =>
      updateGameState(gameState.useAbilityWithoutTarget(abilityId))
      sender() ! Success()
    }
  }

  def useAbilityOnCoordinates(playerId: PlayerId, abilityId: AbilityId, target: HexCoordinates, useData: UseData): Unit = {
    val e = AbilityUsedOnCoordinates(id, playerId, abilityId, target, useData)
    persistAndPublish(e) { _ =>
      updateGameState(gameState.useAbilityOnCoordinates(abilityId, target, useData))
      sender() ! Success()
    }
  }

  def useAbilityOnCharacter(playerId: PlayerId, abilityId: AbilityId, target: CharacterId, useData: UseData): Unit = {
    val e = AbilityUsedOnCharacter(id, playerId, abilityId, target, useData)
    persistAndPublish(e) { _ =>
      updateGameState(gameState.useAbilityOnCharacter(abilityId, target, useData))
      sender() ! Success()
    }
  }

  override def receive: Receive = {
    case GetState =>
      sender() ! gameState
    case GetCurrentClock =>
      sender() ! gameState.getCurrentClock()
    case GetStateView(forPlayer) =>
      sender() ! gameState.toView(forPlayer)
    case StartGame(gameStartDependencies) =>
      validate(v().validateStartGame())(() => startGame(gameStartDependencies))
    case Pause(playerId) =>
      validate(v().validatePause(playerId))(() => {
        if (gameState.clock.isRunning)
          pauseGame()
        else
          unpauseGame()
      })
    case Surrender(playerId) =>
      validate(v().validateSurrender(playerId))(() =>
        surrender(playerId)
      )
    case BanCharacters(playerId, characterIds) =>
      validate(v().validateBanCharacters(playerId, characterIds))(() =>
        banCharacters(playerId, characterIds)
      )
    case PickCharacter(playerId, characterId) =>
      validate(v().validatePickCharacter(playerId, characterId))(() =>
        pickCharacter(playerId, characterId)
      )
    case BlindPickCharacters(playerId, characterIds) =>
      validate(v().validateBlindPickCharacters(playerId, characterIds))(() =>
        blindPickCharacters(playerId, characterIds)
      )
    case PlaceCharacters(playerId, coordinatesToCharacterIdMap) =>
      validate(v().validatePlacingCharacters(playerId, coordinatesToCharacterIdMap))(() =>
        placeCharacters(playerId, coordinatesToCharacterIdMap)
      )
    case EndTurn(playerId) =>
      validate(v().validateEndTurn(playerId))(() =>
        endTurn(playerId)
      )
    case PassTurn(playerId, characterId) =>
      validate(v().validatePassTurn(playerId, characterId))(() =>
        passTurn(playerId, characterId)
      )
    case MoveCharacter(playerId, path, characterId) =>
      validate(v().validateBasicMoveCharacter(playerId, path, characterId))(() =>
        moveCharacter(playerId, path, characterId)
      )
    case BasicAttackCharacter(playerId, attackingCharacterId, targetCharacterId) =>
      validate(v().validateBasicAttackCharacter(playerId, attackingCharacterId, targetCharacterId))(() =>
        basicAttackCharacter(playerId, attackingCharacterId, targetCharacterId)
      )
    case UseAbilityWithoutTarget(playerId, abilityId) =>
      validate(v().validateAbilityUseWithoutTarget(playerId, abilityId))(() =>
        useAbilityWithoutTarget(playerId, abilityId)
      )
    case UseAbilityOnCoordinates(playerId, abilityId, target, useData) =>
      validate(v().validateAbilityUseOnCoordinates(playerId, abilityId, target, useData))(() =>
        useAbilityOnCoordinates(playerId, abilityId, target, useData)
      )
    case UseAbilityOnCharacter(playerId, abilityId, target, useData) =>
      validate(v().validateAbilityUseOnCharacter(playerId, abilityId, target, useData))(() =>
        useAbilityOnCharacter(playerId, abilityId, target, useData)
      )
    case CharacterSelectTimeout(pickNumber) =>
      if(gameState.isInCharacterSelect) {
        handleCharacterSelectTimeout(pickNumber)
      }
    case CharacterPlacingTimeout() =>
      if(gameState.gameStatus == GameStatus.CharacterPlacing) {
        persistAndPublish(CharacterPlacingTimedOut(id))(_ => {
          updateGameStateAndScheduleDefault(gameState.placingCharactersTimeout())
        })
      }
    case TurnTimeout(turnNumber) =>
      if(gameState.turn.number == turnNumber) {
        persistAndPublish(TurnTimedOut(id))(_ =>
          updateGameStateAndScheduleDefault(gameState.surrender(gameState.currentPlayer.id))
        )
      }

    case e => log.warning(s"Unknown message: $e")
  }

  override def receiveRecover: Receive = {
    case GameStarted(_, gameStartDependencies) =>
      updateGameState(gameState.startGame(gameStartDependencies))
      log.debug(s"Recovered game start")
    case Surrendered(_, playerId) =>
      updateGameState(gameState.surrender(playerId))
      log.debug(s"Recovered $playerId surrender")
    case CharactersBanned(_, playerId, characterIds) =>
      updateGameState(gameState.ban(playerId, characterIds))
      log.debug(s"Recovered $playerId ban")
    case CharacterPicked(_, playerId, characterId) =>
      updateGameState(gameState.pick(playerId, characterId))
      log.debug(s"Recovered $playerId pick")
    case CharactersBlindPicked(_, playerId, characterIds) =>
      updateGameState(gameState.blindPick(playerId, characterIds))
      log.debug(s"Recovered $playerId blind pick")
    case PlacingCharactersStarted(_) =>
      updateGameState(gameState.startPlacingCharacters())
      log.debug(s"Recovered start of character placing")
    case CharactersPlaced(_, playerId, coordinatesToCharacterIdMap) =>
      updateGameState(gameState.placeCharacters(playerId, coordinatesToCharacterIdMap))
      log.debug(s"Recovered placing characters by $playerId to $coordinatesToCharacterIdMap")
    case TurnEnded(_, _) =>
      updateGameState(gameState.endTurn())
      log.debug(s"Recovered turn end")
    case TurnPassed(_, _, characterId) =>
      updateGameState(gameState.passTurn(characterId))
      log.debug(s"Recovered turn pass")
    case CharacterMoved(_, _, hexCoordinates, characterId) =>
      updateGameState(gameState.basicMoveCharacter(characterId, hexCoordinates))
      log.debug(s"Recovered $characterId to $hexCoordinates")
    case CharacterBasicAttacked(_, _, attackingCharacterId, targetCharacterId) =>
      updateGameState(gameState.basicAttack(attackingCharacterId, targetCharacterId))
      log.debug(s"Recovered basic attack of $attackingCharacterId to $targetCharacterId")
    case AbilityUsedWithoutTarget(_, _, abilityId) =>
      updateGameState(gameState.useAbilityWithoutTarget(abilityId))
      log.debug(s"Recovered ability $abilityId use without target")
    case AbilityUsedOnCoordinates(_, _, abilityId, target, useData) =>
      updateGameState(gameState.useAbilityOnCoordinates(abilityId, target, useData))
      log.debug(s"Recovered ability $abilityId use on coordinates $target")
    case AbilityUsedOnCharacter(_, _, abilityId, target, useData) =>
      updateGameState(gameState.useAbilityOnCharacter(abilityId, target, useData))
      log.debug(s"Recovered ability $abilityId use on character $target")
    case BanningPhaseTimedOut(_) =>
      log.debug(s"Recovered banning phase timeout")
      updateGameState(gameState.finishBanningPhase())
    case DraftPickTimedOut(_) =>
      log.debug(s"Recovered draft pick timeout")
      updateGameState(gameState.draftPickTimeout())
    case BlindPickTimedOut(_) =>
      log.debug(s"Recovered blind pick timeout")
      updateGameState(gameState.blindPickTimeout())
    case CharacterPlacingTimedOut(_) =>
      log.debug(s"Recovered character placing timeout")
      updateGameState(gameState.placingCharactersTimeout())
    case TurnTimedOut(_) =>
      log.debug(s"Recovered turn timeout")
      updateGameState(gameState.surrender(gameState.currentPlayer.id))
    case RecoveryCompleted =>
      // start a timer
      scheduleDefault()
    case e => log.error(s"Unknown message: $e")
  }

  override def receiveCommand: Receive = {
    case _ =>
  }
}