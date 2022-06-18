package com.tosware.NKM.actors

import akka.actor.{ActorLogging, Props}
import akka.persistence.{PersistentActor, RecoveryCompleted}
import com.tosware.NKM.models.CommandResponse._
import com.tosware.NKM.models.GameStateValidator
import com.tosware.NKM.models.game.NKMCharacter.CharacterId
import com.tosware.NKM.models.game.NKMCharacterMetadata.CharacterMetadataId
import com.tosware.NKM.models.game.Player.PlayerId
import com.tosware.NKM.models.game._
import com.tosware.NKM.models.game.blindpick.BlindPickPhase
import com.tosware.NKM.models.game.draftpick.DraftPickPhase
import com.tosware.NKM.services.NKMDataService

import java.time.Instant
import java.time.temporal.ChronoUnit
import scala.concurrent.duration._
import scala.util.Random

object Game {
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

  case class PlaceCharacter(hexCoordinates: HexCoordinates, characterId: CharacterId) extends Command

  case class MoveCharacter(hexCoordinates: HexCoordinates, characterId: CharacterId) extends Command

  // sent only by self /////////
  //
  case class CharacterSelectTimeout(pickNumber: Int) extends Command

  case class EndTurnTimeout(turnNumber: Int) extends Command
  //////////////////////////////

  sealed trait Event {
    val id: String
  }
  case class TimeDecreased(id: String, playerId: String, time: Long) extends Event

  case class GamePaused(id: String) extends Event

  case class GameUnpaused(id: String) extends Event

  case class GameStarted(id: String, gameStartDependencies: GameStartDependencies) extends Event

  case class Surrendered(id: String, playerId: PlayerId) extends Event

  case class CharactersBanned(id: String, playerId: PlayerId, characterIds: Set[CharacterMetadataId]) extends Event

  case class CharacterPicked(id: String, playerId: PlayerId, characterId: CharacterMetadataId) extends Event

  case class PlacingCharactersStarted(id: String) extends Event

  case class CharacterPlaced(id: String, hexCoordinates: HexCoordinates, characterId: CharacterId) extends Event

  case class CharactersBlindPicked(id: String, playerId: PlayerId, characterId: Set[CharacterMetadataId]) extends Event

  case class CharacterMoved(id: String, hexCoordinates: HexCoordinates, characterId: CharacterId) extends Event

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

  var gameState: GameState = GameState.empty(id)
  var lastTimestamp = Instant.now()

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
    val clockConfig = gameState.clock.config

    if(Seq(GameStatus.NotStarted, GameStatus.Finished).contains(gameState.gameStatus)) {
      return
    }

    val timeout = gameState.gameStatus match {
      case GameStatus.NotStarted => ???
      case GameStatus.CharacterPick => gameState.pickType match {
        case PickType.AllRandom => ???
        case PickType.DraftPick => gameState.draftPickState.get.pickPhase match {
          case DraftPickPhase.Banning => clockConfig.maxBanTimeMillis.millis
          case DraftPickPhase.Picking => clockConfig.maxPickTimeMillis.millis
          case DraftPickPhase.Finished => ???
        }
        case PickType.BlindPick => gameState.blindPickState.get.pickPhase match {
          case BlindPickPhase.Picking => clockConfig.maxPickTimeMillis.millis
          case BlindPickPhase.Finished => ???
        }
      }
      case GameStatus.CharacterPicked => clockConfig.timeAfterPickMillis.millis
      case GameStatus.CharacterPlacing => gameState.clock.playerTimes(gameState.getCurrentPlayer.id).millis
      case GameStatus.Running => gameState.clock.playerTimes(gameState.getCurrentPlayer.id).millis
      case GameStatus.Finished => ???
    }
    val eventToSchedule = gameState.gameStatus match {
      case GameStatus.NotStarted => ???
      case GameStatus.CharacterPick | GameStatus.CharacterPicked =>
        CharacterSelectTimeout(gameState.timeoutNumber)
      case GameStatus.CharacterPlacing | GameStatus.Running | GameStatus.Finished =>
        EndTurnTimeout(gameState.turn.number)
    }
    context.system.scheduler.scheduleOnce(timeout)(self ! eventToSchedule)
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
      GameStateValidator(gameState).validateStartGame() match {
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
      GameStateValidator(gameState).validatePause(playerId) match {
        case failure @ Failure(_) => sender() ! failure
        case Success(_) =>
          if(gameState.clock.isRunning) {
            val timeToDecrease: Long = millisSinceLastMove()
            val isBlindPickingPhase = gameState.blindPickState.fold(false)(_.pickPhase == BlindPickPhase.Picking)
            val isDraftBanningPhase = gameState.draftPickState.fold(false)(_.pickPhase == DraftPickPhase.Banning)
            val isAfterPickPhase = gameState.gameStatus == GameStatus.CharacterPicked
            val playersToDecreaseTime =
              if(isBlindPickingPhase || isDraftBanningPhase || isAfterPickPhase)
                gameState.players.map(_.id)
              else
                Seq(gameState.getCurrentPlayer.id)

            val es = playersToDecreaseTime.map(p => TimeDecreased(id, p, timeToDecrease)) :+ GamePaused(id)
            persistAndPublishAll(es) { _ =>
              gameState = gameState.decreaseTimeForAll(playersToDecreaseTime, timeToDecrease).pause()
              sender() ! Success()
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
      GameStateValidator(gameState).validateSurrender(playerId) match {
        case failure @ Failure(_) => sender() ! failure
        case Success(_) =>
          val e = Surrendered(id, playerId)
          persistAndPublish(e) { _ =>
            gameState = gameState.surrender(playerId)
            sender() ! Success()
          }
      }
    case BanCharacters(playerId, characterIds) =>
      GameStateValidator(gameState).validateBanCharacters(playerId, characterIds) match {
        case failure @ Failure(_) => sender() ! failure
        case Success(_) =>
          val e = CharactersBanned(id, playerId, characterIds)
          persistAndPublish(e) { _ =>
            gameState = gameState.ban(playerId, characterIds)
            sender() ! Success()
          }
      }
    case PickCharacter(playerId, characterId) =>
      GameStateValidator(gameState).validatePickCharacter(playerId, characterId) match {
        case failure @ Failure(_) => sender() ! failure
        case Success(_) =>
          val e = CharacterPicked(id, playerId, characterId)
          persistAndPublish(e) { _ =>
            gameState = gameState.pick(playerId, characterId)
            sender() ! Success()
          }
      }
    case BlindPickCharacters(playerId, characterIds) =>
      GameStateValidator(gameState).validateBlindPickCharacters(playerId, characterIds) match {
        case failure @ Failure(_) => sender() ! failure
        case Success(_) =>
          val e = CharactersBlindPicked(id, playerId, characterIds)
          persistAndPublish(e) { _ =>
            gameState = gameState.blindPick(playerId, characterIds)
            sender() ! Success()
          }
      }
    case PlaceCharacter(hexCoordinates, characterId) =>
      val e = CharacterPlaced(id, hexCoordinates, characterId)
      persistAndPublish(e) { _ =>
        gameState = gameState.placeCharacter(hexCoordinates, characterId)
        sender() ! Success()
      }
    case MoveCharacter(hexCoordinates, characterId) =>
      val e = CharacterMoved(id, hexCoordinates, characterId)
      persistAndPublish(e) { _ =>
        gameState = gameState.moveCharacter(hexCoordinates, characterId)
        sender() ! Success()
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
    case CharacterPlaced(_, hexCoordinates, characterId) =>
      gameState = gameState.placeCharacter(hexCoordinates, characterId)
      log.debug(s"Recovered $characterId on $hexCoordinates")
    case CharacterMoved(_, hexCoordinates, characterId) =>
      gameState = gameState.moveCharacter(hexCoordinates, characterId)
      log.debug(s"Recovered $characterId to $hexCoordinates")
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
    case e => log.warning(s"Unknown message: $e")
  }

  override def receiveCommand: Receive = {
    case _ =>
  }
}