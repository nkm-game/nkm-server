package com.tosware.NKM.actors

import akka.actor.{ActorLogging, Props}
import akka.persistence.PersistentActor
import com.softwaremill.quicklens._
import com.tosware.NKM.models._

object Game {
  sealed trait Command
  case object GetState extends Command

  case class AddPlayer(name: String) extends Command
  case class AddCharacter(playerName: String, character: NKMCharacter) extends Command
  case class PlaceCharacter(hexCoordinates: HexCoordinates, character: NKMCharacter) extends Command
  case class MoveCharacter(hexCoordinates: HexCoordinates, character: NKMCharacter) extends Command

  sealed trait Event
  case class PlayerAdded(name: String) extends Event
  case class CharacterAdded(playerName: String, character: NKMCharacter) extends Event
  case class CharacterPlaced(hexCoordinates: HexCoordinates, character: NKMCharacter) extends Event
  case class CharacterMoved(hexCoordinates: HexCoordinates, character: NKMCharacter) extends Event

  def props(id: String, hexMap: HexMap): Props = Props(new Game(id, hexMap))
}

class Game(id: String, hexMap: HexMap) extends PersistentActor with ActorLogging {
  import Game._
  var gameState: GameState = GameState(hexMap)

  def placeCharacter(targetCellCoordinates: HexCoordinates, character: NKMCharacter): Unit =
      gameState = gameState.modify(_.hexMap.cells.each).using {
        case cell if cell.coordinates == targetCellCoordinates => HexCell(cell.coordinates, cell.cellType, Some(character), cell.effects, cell.spawnNumber)
        case cell => cell
      }.modify(_.charactersOutsideMap).using(_.filter(_ != character))

  def moveCharacter(parentCellCoordinates: HexCoordinates, character: NKMCharacter): Unit = {
    val parentCell = gameState.hexMap.cells.find(_.character.contains(character)).getOrElse {
      log.error(s"Unable to move character ${character.name} to $parentCellCoordinates")
      return
    }
    gameState = gameState.modify(_.hexMap.cells.each).using {
      case cell if cell == parentCell => HexCell(cell.coordinates, cell.cellType, None, cell.effects, cell.spawnNumber)
      case cell if cell.coordinates == parentCellCoordinates => HexCell(cell.coordinates, cell.cellType, Some(character), cell.effects, cell.spawnNumber)
      case cell => cell
    }
  }

  def addPlayer(name: String): Unit =
    gameState = gameState.modify(_.players).setTo(Player(name) :: gameState.players)

  def addCharacter(playerName: String, character: NKMCharacter): Unit = {
    val currentCharacters = gameState.players.find(_.name == playerName).getOrElse {
      log.error(s"Player ${playerName} not found")
      return
    }.characters
    gameState = gameState.modify(_.players.each).using {
      case p if p.name == playerName => p.modify(_.characters).setTo(character :: currentCharacters)
      case p => p
    }.modify(_.charactersOutsideMap).setTo(character :: gameState.charactersOutsideMap)
  }

  override def persistenceId: String = s"game-$id"
  override def receive: Receive = {
    case GetState =>
      log.info("Received state request")
      sender ! gameState
    case AddPlayer(name) =>
      log.info(s"Adding player: $name")
      persist(PlayerAdded(name)) { _ =>
        addPlayer(name)
        log.info(s"Persisted player: $name")
      }
    case AddCharacter(player, character) =>
      log.info(s"Adding character: ${character.name}")
      persist(CharacterAdded(player, character)) { _ =>
        addCharacter(player, character)
        log.info(s"Persisted character: ${character.name}")
      }
    case PlaceCharacter(hexCoordinates, character) =>
      log.info(s"Placing ${character.name} on $hexCoordinates")
      persist(CharacterPlaced(hexCoordinates, character)) { _ =>
        placeCharacter(hexCoordinates, character)
        log.info(s"Persisted ${character.name} on $hexCoordinates")
      }
    case MoveCharacter(hexCoordinates, character) =>
      log.info(s"Moving ${character.name} to $hexCoordinates")
      persist(CharacterMoved(hexCoordinates, character)) { _ =>
        moveCharacter(hexCoordinates, character)
        log.info(s"Persisted ${character.name} on $hexCoordinates")
      }
    case e => log.warning(s"Unknown message: $e")
  }

  override def receiveRecover: Receive = {
    case PlayerAdded(name) =>
      addPlayer(name)
      log.info(s"Recovered player: $name")
    case CharacterAdded(player, character) =>
      addCharacter(player, character)
      log.info(s"Recovered character: ${character.name}")
    case CharacterPlaced(hexCoordinates, character) =>
      placeCharacter(hexCoordinates, character)
      log.info(s"Recovered ${character.name} on $hexCoordinates")
    case CharacterMoved(hexCoordinates, character) =>
      moveCharacter(hexCoordinates, character)
      log.info(s"Recovered ${character.name} to $hexCoordinates")
    case e => log.warning(s"Unknown message: $e")
  }

  override def receiveCommand: Receive = {
    case _ =>
  }
}