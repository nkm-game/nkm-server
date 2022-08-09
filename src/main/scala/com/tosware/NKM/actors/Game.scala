package com.tosware.NKM.actors

import akka.actor.{ActorLogging, Cancellable, Props}
import akka.persistence.{PersistentActor, RecoveryCompleted}
import com.tosware.NKM.models.CommandResponse._
import com.tosware.NKM.models.GameStateValidator
import com.tosware.NKM.models.game.NKMCharacter.CharacterId
import com.tosware.NKM.models.game.CharacterMetadata.CharacterMetadataId
import com.tosware.NKM.models.game.Player.PlayerId
import com.tosware.NKM.models.game._
import com.tosware.NKM.models.game.blindpick.BlindPickPhase
import com.tosware.NKM.models.game.draftpick.DraftPickPhase
import com.tosware.NKM.models.game.hex.HexCoordinates
import com.tosware.NKM.services.NKMDataService

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

  case class MoveCharacter(playerId: PlayerId, path: Seq[HexCoordinates], characterId: CharacterId) extends Command

  case class BasicAttackCharacter(playerId: PlayerId, attackingCharacterId: CharacterId, targetCharacterId: CharacterId) extends Command

  // sent only by self /////////
  //
  case class CharacterSelectTimeout(pickNumber: Int) extends Command

  case class EndTurnTimeout(turnNumber: Int) extends Command
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

  case class CharacterMoved(id: GameId, playerId: PlayerId, path: Seq[HexCoordinates], characterId: CharacterId) extends Event

  case class CharacterBasicAttacked(id: GameId, playerId: PlayerId, attackingCharacterId: CharacterId, targetCharacterId: CharacterId) extends Event

  // CLOCK TIMEOUTS /////////////////////
  //
  case class BanningPhaseTimedOut(id: String) extends Event

  case class DraftPickTimedOut(id: String) extends Event

  case class BlindPickTimedOut(id: String) extends Event

  case class TimeAfterPickTimedOut(id: String) extends Event

  case class TurnTimedOut(id: String) extends Event
  //////////////////////////////


  def props(id: String)(implicit NKMDataService: NKMDataService): Props = Props(new Game(id))
}

class Game(id: String)(implicit NKMDataService: NKMDataService) extends PersistentActor with ActorLogging {
  import Game._
  import context.dispatcher

  override def persistenceId: String = s"game-$id"

  implicit val random: Random = new Random(id.hashCode)

  implicit var gameState: GameState = GameState.empty(id)
  var lastTimestamp: Instant = Instant.now()
  var scheduledTimeout: Cancellable = Cancellable.alreadyCancelled

  def updateTimestamp(): Unit = lastTimestamp = Instant.now()

  def millisSinceLastMove(): Long = ChronoUnit.MILLIS.between(lastTimestamp, Instant.now())

  def persistAndPublishAll[A](events: Seq[A])(handler: A => Unit): Unit = {
    log.warning("EVENT " + events.toString)
    persistAll(events)(handler)
    events.foreach(context.system.eventStream.publish)
  }

  def persistAndPublish[A](event: A)(handler: A => Unit): Unit = {
    log.warning("EVENT " + event.toString)
    persist(event)(handler)
    context.system.eventStream.publish(event)
  }

  def scheduleDefault(): Unit = {
    if(Seq(GameStatus.NotStarted, GameStatus.Finished).contains(gameState.gameStatus))
      return

    val timeout = gameState.gameStatus match {
      case GameStatus.NotStarted | GameStatus.Finished => ???
      case GameStatus.CharacterPick | GameStatus.CharacterPicked =>
        gameState.clock.pickTime.millis
      case GameStatus.CharacterPlacing | GameStatus.Running =>
        gameState.currentPlayerTime.millis
    }

    val eventToSchedule: Command = gameState.gameStatus match {
      case GameStatus.NotStarted | GameStatus.Finished => ???
      case GameStatus.CharacterPick | GameStatus.CharacterPicked =>
        CharacterSelectTimeout(gameState.timeoutNumber)
      case GameStatus.CharacterPlacing | GameStatus.Running =>
        EndTurnTimeout(gameState.turn.number)
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
            gameState = gameState.startGame(gameStartDependencies)
            sender() ! Success()
            scheduleDefault()
          }
      }
    case CharacterSelectTimeout(pickNumber) =>
      if(Seq(GameStatus.CharacterPick, GameStatus.CharacterPicked).contains(gameState.gameStatus)) {
        def startPlacingCharactersAfterTimeout(): Unit =
          persistAndPublishAll(Seq(TimeAfterPickTimedOut(id), PlacingCharactersStarted(id))) { _ =>
            gameState = gameState.startPlacingCharacters()
          }

        def banningPhaseTimeout(): Unit =
          persistAndPublish(BanningPhaseTimedOut(id))(_ => gameState = gameState.finishBanningPhase())

        def draftPickTimeout(): Unit =
          persistAndPublish(DraftPickTimedOut(id))(_ => gameState = gameState.draftPickTimeout())

        def blindPickTimeout(): Unit =
          persistAndPublish(BlindPickTimedOut(id)) { _ =>
            gameState = gameState.blindPickTimeout()
          }

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
                gameState = gameState.decreasePickTime(timeToDecrease).pause()
                sender() ! Success()
              }
            } else {
              persistAndPublishAll(Seq(TimeDecreased(id, gameState.currentPlayer.id, timeToDecrease), GamePaused(id))) { _ =>
                scheduledTimeout.cancel()
                gameState = gameState.decreaseTime(gameState.currentPlayer.id, timeToDecrease).pause()
                sender() ! Success()
              }
            }
          } else {
            val e = GameUnpaused(id)
            persistAndPublish(e) { _ =>
              gameState = gameState.unpause()
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
            gameState = gameState.surrender(playerId)
            sender() ! Success()
          }
      }
    case BanCharacters(playerId, characterIds) =>
      GameStateValidator().validateBanCharacters(playerId, characterIds) match {
        case failure @ Failure(_) => sender() ! failure
        case Success(_) =>
          val e = CharactersBanned(id, playerId, characterIds)
          persistAndPublish(e) { _ =>
            gameState = gameState.ban(playerId, characterIds)
            sender() ! Success()
          }
      }
    case PickCharacter(playerId, characterId) =>
      GameStateValidator().validatePickCharacter(playerId, characterId) match {
        case failure @ Failure(_) => sender() ! failure
        case Success(_) =>
          val e = CharacterPicked(id, playerId, characterId)
          persistAndPublish(e) { _ =>
            gameState = gameState.pick(playerId, characterId)
            sender() ! Success()
          }
      }
    case BlindPickCharacters(playerId, characterIds) =>
      GameStateValidator().validateBlindPickCharacters(playerId, characterIds) match {
        case failure @ Failure(_) => sender() ! failure
        case Success(_) =>
          val e = CharactersBlindPicked(id, playerId, characterIds)
          persistAndPublish(e) { _ =>
            gameState = gameState.blindPick(playerId, characterIds)
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
            gameState = gameState.placeCharacters(playerId, coordinatesToCharacterIdMap)
            sender() ! Success()
          }
      }
    case EndTurn(playerId) =>
      GameStateValidator().validateEndTurn(playerId) match {
        case failure @ Failure(_) => sender() ! failure
        case Success(_) =>
          val e = TurnEnded(id, playerId)
          persistAndPublish(e) { _ =>
            gameState = gameState.endTurn()
            sender() ! Success()
          }
      }
    case MoveCharacter(playerId, path, characterId) =>
      GameStateValidator().validateBasicMoveCharacter(playerId, path, characterId) match {
        case failure @ Failure(_) => sender() ! failure
        case Success(_) =>
          val e = CharacterMoved(id, playerId, path, characterId)
          persistAndPublish(e) { _ =>
            gameState = gameState.basicMoveCharacter(playerId, path, characterId)
            sender() ! Success()
          }
      }
    case BasicAttackCharacter(playerId, attackingCharacterId, targetCharacterId) =>
      GameStateValidator().validateBasicAttackCharacter(playerId, attackingCharacterId, targetCharacterId) match {
        case failure @ Failure(_) => sender() ! failure
        case Success(_) =>
          val e = CharacterBasicAttacked(id, playerId, attackingCharacterId, targetCharacterId)
          persistAndPublish(e) { _ =>
            gameState = gameState.basicAttack(attackingCharacterId, targetCharacterId)
            sender() ! Success()
          }
      }
    case e => log.warning(s"Unknown message: $e")
  }

  override def receiveRecover: Receive = {
    case GameStarted(_, gameStartDependencies) =>
      gameState = gameState.startGame(gameStartDependencies)
      log.debug(s"Recovered game start")
    case Surrendered(_, playerId) =>
      gameState = gameState.surrender(playerId)
      log.debug(s"Recovered $playerId surrender")
    case CharactersBanned(_, playerId, characterIds) =>
      gameState = gameState.ban(playerId, characterIds)
      log.debug(s"Recovered $playerId ban")
    case CharacterPicked(_, playerId, characterId) =>
      gameState = gameState.pick(playerId, characterId)
      log.debug(s"Recovered $playerId pick")
    case CharactersBlindPicked(_, playerId, characterIds) =>
      gameState = gameState.blindPick(playerId, characterIds)
      log.debug(s"Recovered $playerId blind pick")
    case PlacingCharactersStarted(_) =>
      gameState = gameState.startPlacingCharacters()
      log.debug(s"Recovered start of character placing")
    case CharactersPlaced(_, playerId, coordinatesToCharacterIdMap) =>
      gameState = gameState.placeCharacters(playerId, coordinatesToCharacterIdMap)
      log.debug(s"Recovered placing characters by $playerId to $coordinatesToCharacterIdMap")
    case TurnEnded(_, _) =>
      gameState = gameState.endTurn()
      log.debug(s"Recovered turn end")
    case CharacterMoved(_, playerId, hexCoordinates, characterId) =>
      gameState = gameState.basicMoveCharacter(playerId, hexCoordinates, characterId)
      log.debug(s"Recovered $characterId to $hexCoordinates")
    case CharacterBasicAttacked(_, _, attackingCharacterId, targetCharacterId) =>
      gameState = gameState.basicAttack(attackingCharacterId, targetCharacterId)
      log.debug(s"Recovered basic attack of $attackingCharacterId to $targetCharacterId")
    case BanningPhaseTimedOut(_) =>
      log.debug(s"Recovered banning phase timeout")
      gameState = gameState.finishBanningPhase()
    case DraftPickTimedOut(_) =>
      log.debug(s"Recovered draft pick timeout")
      gameState = gameState.draftPickTimeout()
    case BlindPickTimedOut(_) =>
      log.debug(s"Recovered blind pick timeout")
      gameState = gameState.blindPickTimeout()
    case RecoveryCompleted =>
      // start a timer
      scheduleDefault()
    case e => log.error(s"Unknown message: $e")
  }

  override def receiveCommand: Receive = {
    case _ =>
  }
}