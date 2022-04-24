package com.tosware.NKM.actors

import akka.actor.{ActorLogging, Props}
import akka.persistence.{PersistentActor, RecoveryCompleted}
import com.tosware.NKM.models.CommandResponse._
import com.tosware.NKM.models.GameStateValidator
import com.tosware.NKM.models.game.NKMCharacter.CharacterId
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

  case class PickCharacter(playerId: PlayerId, characterId: CharacterMetadataId) extends Command

  case class BlindPickCharacters(playerId: PlayerId, characterId: Set[CharacterMetadataId]) extends Command

  case class PlaceCharacter(hexCoordinates: HexCoordinates, characterId: CharacterId) extends Command

  case class MoveCharacter(hexCoordinates: HexCoordinates, characterId: CharacterId) extends Command

  sealed trait Event {
    val id: String
  }

  case class GameStarted(id: String, gameStartDependencies: GameStartDependencies) extends Event

  case class Surrendered(id: String, playerId: PlayerId) extends Event

  case class CharactersBanned(id: String, playerId: PlayerId, characterIds: Set[CharacterMetadataId]) extends Event

  case class CharacterPicked(id: String, playerId: PlayerId, characterId: CharacterMetadataId) extends Event

  case class CharacterPlaced(id: String, hexCoordinates: HexCoordinates, characterId: CharacterId) extends Event

  case class CharactersBlindPicked(id: String, playerId: PlayerId, characterId: Set[CharacterMetadataId]) extends Event

  case class CharacterMoved(id: String, hexCoordinates: HexCoordinates, characterId: CharacterId) extends Event

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