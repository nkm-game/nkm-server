package com.tosware.NKM.actors

import akka.actor.{ActorLogging, Props}
import akka.persistence.{PersistentActor, RecoveryCompleted}
import com.tosware.NKM.models.CommandResponse._
import com.tosware.NKM.models.GameStateValidator
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
      log.warning(gameState.toString)
      sender() ! gameState
    case StartGame(gameStartDependencies) =>
      GameStateValidator(gameState).validateStartGame() match {
        case failure @ Failure(_) => sender() ! failure
        case Success(_) =>
          val e = GameStarted(id, gameStartDependencies)
          persistAndPublish(e) { _ =>
            gameState = gameState.startGame(gameStartDependencies)
            sender() ! Success()
          }
      }
    case Surrender(playerName) =>
      GameStateValidator(gameState).validateSurrender(playerName) match {
        case failure @ Failure(_) => sender() ! failure
        case Success(_) =>
          val e = Surrendered(id, playerName)
          persistAndPublish(e) { _ =>
            gameState = gameState.surrender(playerName)
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