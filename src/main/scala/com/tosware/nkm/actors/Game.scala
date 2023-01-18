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

import java.time.Instant
import java.time.temporal.ChronoUnit
import scala.concurrent.duration._
import scala.util.Random

object Game {
  type GameId = String
  sealed trait Query

  case object GetState extends Query

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
  case class PickTimeDecreased(id: GameId, time: Long) extends Event

  case class TimeDecreased(id: GameId, playerId: PlayerId, time: Long) extends Event

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
  var lastTimestamp: Instant = Instant.now()
  var scheduledTimeout: Cancellable = Cancellable.alreadyCancelled

  def updateGameState(newGameState: GameState): Unit = {
    val lastGameState = gameState
    gameState = newGameState

    val newGameEvents = newGameState.gameLog.events.drop(lastGameState.gameLog.events.size).map(e => GameEventMapped(id, e))

    newGameEvents.foreach(context.system.eventStream.publish)
  }

  def updateTimestamp(): Unit = lastTimestamp = Instant.now()

  def millisSinceLastMove(): Long = ChronoUnit.MILLIS.between(lastTimestamp, Instant.now())

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

    val timeout = gameState.gameStatus match {
      case GameStatus.NotStarted | GameStatus.Finished => ???
      case GameStatus.CharacterPick | GameStatus.CharacterPicked | GameStatus.CharacterPlacing =>
        gameState.clock.sharedTime.millis
      case GameStatus.Running =>
        gameState.currentPlayerTime.millis
    }

    val eventToSchedule: Command = gameState.gameStatus match {
      case GameStatus.NotStarted | GameStatus.Finished => ???
      case GameStatus.CharacterPick | GameStatus.CharacterPicked =>
        CharacterSelectTimeout(gameState.timeoutNumber)
      case GameStatus.CharacterPlacing =>
        CharacterPlacingTimeout()
      case GameStatus.Running =>
        TurnTimeout(gameState.turn.number)
    }
    scheduledTimeout.cancel()
    scheduledTimeout = context.system.scheduler.scheduleOnce(timeout)(self ! eventToSchedule)
    updateTimestamp()
  }

  override def receive: Receive = {
    case GetState =>
      log.debug(s"GAME STATE REQUEST: ${gameState.toString}")
      sender() ! gameState
    case GetStateView(forPlayer) =>
      val gameStateView = gameState.toView(forPlayer)
      log.debug(s"GAME STATE VIEW REQUEST: ${gameStateView.toString}")
      sender() ! gameStateView
    case StartGame(gameStartDependencies) =>
      GameStateValidator().validateStartGame() match {
        case failure @ Failure(_) => sender() ! failure
        case Success(_) =>
          val e = GameStarted(id, gameStartDependencies)
          persistAndPublish(e) { _ =>
            updateGameState(gameState.startGame(gameStartDependencies))
            sender() ! Success()
            scheduleDefault()
          }
      }
    case CharacterSelectTimeout(pickNumber) =>
      if(Seq(GameStatus.CharacterPick, GameStatus.CharacterPicked).contains(gameState.gameStatus)) {
        def startPlacingCharactersAfterTimeout(): Unit =
          persistAndPublishAll(Seq(TimeAfterPickTimedOut(id), PlacingCharactersStarted(id))) { _ =>
            updateGameState(gameState.startPlacingCharacters())
          }

        def banningPhaseTimeout(): Unit =
          persistAndPublish(BanningPhaseTimedOut(id))(_ => updateGameState(gameState.finishBanningPhase()))

        def draftPickTimeout(): Unit =
          persistAndPublish(DraftPickTimedOut(id))(_ => updateGameState(gameState.draftPickTimeout()))

        def blindPickTimeout(): Unit =
          persistAndPublish(BlindPickTimedOut(id))(_ => updateGameState(gameState.blindPickTimeout()))

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
      scheduleDefault()
    case CharacterPlacingTimeout() =>
      persistAndPublish(CharacterPlacingTimedOut(id))(_ => updateGameState(gameState.placingCharactersTimeout()))
      scheduleDefault()
    case TurnTimeout(turnNumber) =>
      if(gameState.turn.number == turnNumber) {
        persistAndPublish(TurnTimedOut(id))(_ => updateGameState(gameState.surrender(gameState.currentPlayer.id)))
        scheduleDefault()
      }
    case Pause(playerId) =>
      GameStateValidator().validatePause(playerId) match {
        case failure @ Failure(_) => sender() ! failure
        case Success(_) =>
          if(gameState.clock.isRunning) {
            val timeToDecrease: Long = millisSinceLastMove()
            val isBlindPickingPhase = gameState.blindPickState.fold(false)(_.pickPhase == BlindPickPhase.Picking)
            val isDraftBanningPhase = gameState.draftPickState.fold(false)(_.pickPhase == DraftPickPhase.Banning)
            val isAfterPickPhase = gameState.gameStatus == GameStatus.CharacterPicked
            val isPickTime = isBlindPickingPhase || isDraftBanningPhase || isAfterPickPhase

            if(isPickTime) {
              persistAndPublishAll(Seq(PickTimeDecreased(id, timeToDecrease), GamePaused(id))) { _ =>
                scheduledTimeout.cancel()
                updateGameState(gameState.decreasePickTime(timeToDecrease).pause())
                sender() ! Success()
              }
            } else {
              persistAndPublishAll(Seq(TimeDecreased(id, gameState.currentPlayer.id, timeToDecrease), GamePaused(id))) { _ =>
                scheduledTimeout.cancel()
                updateGameState(gameState.decreaseTime(gameState.currentPlayer.id, timeToDecrease).pause())
                sender() ! Success()
              }
            }
          } else {
            val e = GameUnpaused(id)
            persistAndPublish(e) { _ =>
              updateGameState(gameState.unpause())
              sender() ! Success()
              scheduleDefault()
            }
          }
      }
    case Surrender(playerId) =>
      GameStateValidator().validateSurrender(playerId) match {
        case failure @ Failure(_) => sender() ! failure
        case Success(_) =>
          val e = Surrendered(id, playerId)
          persistAndPublish(e) { _ =>
            updateGameState(gameState.surrender(playerId))
            sender() ! Success()
          }
      }
    case BanCharacters(playerId, characterIds) =>
      GameStateValidator().validateBanCharacters(playerId, characterIds) match {
        case failure @ Failure(_) => sender() ! failure
        case Success(_) =>
          val e = CharactersBanned(id, playerId, characterIds)
          persistAndPublish(e) { _ =>
            updateGameState(gameState.ban(playerId, characterIds))
            sender() ! Success()
          }
      }
    case PickCharacter(playerId, characterId) =>
      GameStateValidator().validatePickCharacter(playerId, characterId) match {
        case failure @ Failure(_) => sender() ! failure
        case Success(_) =>
          val e = CharacterPicked(id, playerId, characterId)
          persistAndPublish(e) { _ =>
            updateGameState(gameState.pick(playerId, characterId))
            sender() ! Success()
          }
      }
    case BlindPickCharacters(playerId, characterIds) =>
      GameStateValidator().validateBlindPickCharacters(playerId, characterIds) match {
        case failure @ Failure(_) => sender() ! failure
        case Success(_) =>
          val e = CharactersBlindPicked(id, playerId, characterIds)
          persistAndPublish(e) { _ =>
            updateGameState(gameState.blindPick(playerId, characterIds))
            sender() ! Success()
            if(gameState.characterPickFinished)
              scheduleDefault()
          }
      }
    case PlaceCharacters(playerId, coordinatesToCharacterIdMap) =>
      GameStateValidator().validatePlacingCharacters(playerId, coordinatesToCharacterIdMap) match {
        case failure @ Failure(_) => sender() ! failure
        case Success(_) =>
          val e = CharactersPlaced(id, playerId, coordinatesToCharacterIdMap)
          persistAndPublish(e) { _ =>
            updateGameState(gameState.placeCharacters(playerId, coordinatesToCharacterIdMap))
            sender() ! Success()
          }
      }
    case EndTurn(playerId) =>
      GameStateValidator().validateEndTurn(playerId) match {
        case failure @ Failure(_) => sender() ! failure
        case Success(_) =>
          val e = TurnEnded(id, playerId)
          persistAndPublish(e) { _ =>
            updateGameState(gameState.endTurn())
            sender() ! Success()
          }
      }
    case PassTurn(playerId, characterId) =>
      GameStateValidator().validatePassTurn(playerId, characterId) match {
        case failure @ Failure(_) => sender() ! failure
        case Success(_) =>
          val e = TurnPassed(id, playerId, characterId)
          persistAndPublish(e) { _ =>
            updateGameState(gameState.passTurn(characterId))
            sender() ! Success()
          }
      }
    case MoveCharacter(playerId, path, characterId) =>
      GameStateValidator().validateBasicMoveCharacter(playerId, path, characterId) match {
        case failure @ Failure(_) => sender() ! failure
        case Success(_) =>
          val e = CharacterMoved(id, playerId, path, characterId)
          persistAndPublish(e) { _ =>
            updateGameState(gameState.basicMoveCharacter(characterId, path))
            sender() ! Success()
          }
      }
    case BasicAttackCharacter(playerId, attackingCharacterId, targetCharacterId) =>
      GameStateValidator().validateBasicAttackCharacter(playerId, attackingCharacterId, targetCharacterId) match {
        case failure @ Failure(_) => sender() ! failure
        case Success(_) =>
          val e = CharacterBasicAttacked(id, playerId, attackingCharacterId, targetCharacterId)
          persistAndPublish(e) { _ =>
            updateGameState(gameState.basicAttack(attackingCharacterId, targetCharacterId))
            sender() ! Success()
          }
      }
    case UseAbilityWithoutTarget(playerId, abilityId) =>
      GameStateValidator().validateAbilityUseWithoutTarget(playerId, abilityId) match {
        case failure @ Failure(_) => sender() ! failure
        case Success(_) =>
          val e = AbilityUsedWithoutTarget(id, playerId, abilityId)
          persistAndPublish(e) { _ =>
            updateGameState(gameState.useAbilityWithoutTarget(abilityId))
            sender() ! Success()
          }
      }
    case UseAbilityOnCoordinates(playerId, abilityId, target, useData) =>
      GameStateValidator().validateAbilityUseOnCoordinates(playerId, abilityId, target, useData) match {
        case failure @ Failure(_) => sender() ! failure
        case Success(_) =>
          val e = AbilityUsedOnCoordinates(id, playerId, abilityId, target, useData)
          persistAndPublish(e) { _ =>
            updateGameState(gameState.useAbilityOnCoordinates(abilityId, target, useData))
            sender() ! Success()
          }
      }
    case UseAbilityOnCharacter(playerId, abilityId, target, useData) =>
      GameStateValidator().validateAbilityUseOnCharacter(playerId, abilityId, target, useData) match {
        case failure @ Failure(_) => sender() ! failure
        case Success(_) =>
          val e = AbilityUsedOnCharacter(id, playerId, abilityId, target, useData)
          persistAndPublish(e) { _ =>
            updateGameState(gameState.useAbilityOnCharacter(abilityId, target, useData))
            sender() ! Success()
          }
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