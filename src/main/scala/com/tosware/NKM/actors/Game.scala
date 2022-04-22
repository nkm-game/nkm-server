package com.tosware.NKM.actors

import akka.actor.{ActorLogging, Props}
import akka.persistence.{PersistentActor, RecoveryCompleted}
import com.tosware.NKM.models.CommandResponse._
import com.tosware.NKM.models.game.GamePhase._
import com.tosware.NKM.models.game.NKMCharacterMetadata.CharacterMetadataId
import com.tosware.NKM.models.game.Player.PlayerId
import com.tosware.NKM.models.game._
import com.tosware.NKM.services.NKMDataService

import scala.util.Random

object Game {
  sealed trait Query

  case object GetState extends Query

  sealed trait Command

  case class StartGame(gameStartDependencies: GameStartDependencies) extends Command

  case class Surrender(playerId: PlayerId) extends Command

  case class BanCharacters(playerId: PlayerId, characterIds: Set[CharacterMetadataId]) extends Command

  case class PlaceCharacter(hexCoordinates: HexCoordinates, characterId: String) extends Command

  case class MoveCharacter(hexCoordinates: HexCoordinates, characterId: String) extends Command

  sealed trait Event {
    val id: String
  }

  case class GameStarted(id: String, gameStartDependencies: GameStartDependencies) extends Event

  case class Surrendered(id: String, playerId: PlayerId) extends Event

  case class CharactersBanned(id: String, playerId: PlayerId, characterIds: Set[CharacterMetadataId]) extends Event

  case class CharacterPlaced(id: String, hexCoordinates: HexCoordinates, characterId: String) extends Event

  case class CharacterMoved(id: String, hexCoordinates: HexCoordinates, characterId: String) extends Event

  def props(id: String)(implicit NKMDataService: NKMDataService): Props = Props(new Game(id))
}

class Game(id: String)(implicit NKMDataService: NKMDataService) extends PersistentActor with ActorLogging {

  import Game._

  var gameState: GameState = GameState.empty(id)
  implicit val random: Random = new Random(id.hashCode)

  def persistAndPublish[A](event: A)(handler: A => Unit): Unit = {
    context.system.eventStream.publish(event)
    persist(event)(handler)
  }

  override def persistenceId: String = s"game-$id"

  override def receive: Receive = {
    case GetState =>
      log.info("Received state request")
      log.warning(gameState.toString)
      sender() ! gameState
    case StartGame(gameStartDependencies) =>
      log.info(s"Starting the game")
      if (gameState.gamePhase != NotStarted) {
        sender() ! Failure("Game is not started")
      } else {
        val e = GameStarted(id, gameStartDependencies)
        persistAndPublish(e) { _ =>
          gameState = gameState.startGame(gameStartDependencies)
          sender() ! Success()
        }
      }
    case Surrender(playerName) =>
      log.info(s"Surrendering $playerName")
      val playerOption = gameState.players.find(_.name == playerName)
      if (gameState.gamePhase == NotStarted) {
        sender() ! Failure("Game is not started")
      } else if (playerOption.isEmpty) {
        sender() ! Failure("This player is not in this game.")
      } else {
        val player = playerOption.get
        if (player.victoryStatus != VictoryStatus.Pending) {
          sender() ! Failure("This player already finished the game.")
        } else {
          val e = Surrendered(id, playerName)
          persistAndPublish(e) { _ =>
            gameState = gameState.surrender(playerName)
            log.info(s"Surrendered $playerName")
            sender() ! Success()
          }
        }
      }
    case BanCharacters(playerId, characterIds) =>
      log.info(s"$playerId banning")
      val playerOption = gameState.players.find(_.name == playerId)
      if (playerOption.isEmpty) {
        sender() ! Failure("This player is not in this game.")
      } else if (gameState.gamePhase != CharacterPick) {
        sender() ! Failure("Game is not in character pick phase")
      } else if (!gameState.draftPickState.fold(false)(_.validateBan(playerId, characterIds))) {
        sender() ! Failure("Ban is not valid")
      } else {
        val e = CharactersBanned(id, playerId, characterIds)
        persistAndPublish(e) { _ =>
          gameState = gameState.ban(playerId, characterIds)
          log.info(s"$playerId banned")
          sender() ! Success()
        }
      }
    case PlaceCharacter(hexCoordinates, characterId) =>
      log.info(s"Placing $characterId on $hexCoordinates")
      val e = CharacterPlaced(id, hexCoordinates, characterId)
      persistAndPublish(e) { _ =>
        gameState = gameState.placeCharacter(hexCoordinates, characterId)
        log.info(s"Persisted $characterId on $hexCoordinates")
        sender() ! Success()
      }
    case MoveCharacter(hexCoordinates, characterId) =>
      log.info(s"Moving $characterId to $hexCoordinates")
      val e = CharacterMoved(id, hexCoordinates, characterId)
      persistAndPublish(e) { _ =>
        gameState = gameState.moveCharacter(hexCoordinates, characterId)
        log.info(s"Persisted $characterId on $hexCoordinates")
        sender() ! Success()
      }
    case e => log.warning(s"Unknown message: $e")
  }

  override def receiveRecover: Receive = {
    case GameStarted(_, gameStartDependencies) =>
      gameState = gameState.startGame(gameStartDependencies)
      log.debug(s"Recovered game start")
    case Surrendered(_, playerName) =>
      gameState = gameState.surrender(playerName)
      log.debug(s"Recovered $playerName surrender")
    case CharactersBanned(_, playerId, characterIds) =>
      gameState = gameState.ban(playerId, characterIds)
      log.debug(s"Recovered $playerId ban")
    case CharacterPlaced(_, hexCoordinates, characterId) =>
      gameState = gameState.placeCharacter(hexCoordinates, characterId)
      log.debug(s"Recovered $characterId on $hexCoordinates")
    case CharacterMoved(_, hexCoordinates, characterId) =>
      gameState = gameState.moveCharacter(hexCoordinates, characterId)
      log.debug(s"Recovered $characterId to $hexCoordinates")
    case RecoveryCompleted =>
    case e => log.warning(s"Unknown message: $e")
  }

  override def receiveCommand: Receive = {
    case _ =>
  }
}