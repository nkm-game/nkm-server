package com.tosware.NKM.actors

import com.softwaremill.quicklens._
import com.tosware.NKM._
import akka.actor.{Actor, ActorLogging, Props}
import akka.persistence.PersistentActor

object Game {
  sealed trait Command
  case object GetState extends Command

  case class PlaceCharacter(hexCoordinates: HexCoordinates, character: NKMCharacter) extends Command
  case class MoveCharacter(hexCoordinates: HexCoordinates, character: NKMCharacter) extends Command

  sealed trait Event
  case class CharacterPlaced(hexCoordinates: HexCoordinates, character: NKMCharacter) extends Event
  case class CharacterMoved(hexCoordinates: HexCoordinates, character: NKMCharacter) extends Event

  def props(id: String, hexMap: HexMap): Props = Props(new Game(id, hexMap))
}

class Game(id: String, hexMap: HexMap) extends PersistentActor with ActorLogging {
  import Game._
  val charactersOutsideMap: List[NKMCharacter] = List[NKMCharacter](
    NKMCharacter("Aqua", 12, Stat(32), Stat(43), Stat(4), Stat(34), Stat(4)),
    NKMCharacter("Dekomori Sanae", 14, Stat(32), Stat(43), Stat(4), Stat(34), Stat(4)),
    NKMCharacter("Aqua", 0, Stat(34), Stat(43), Stat(4), Stat(34), Stat(5))
  )
  var gameState: GameState = GameState(hexMap, charactersOutsideMap)

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

  override def persistenceId: String = s"game-$id"
  override def receive: Receive = {
    case GetState =>
      log.info("Received state request")
      sender ! gameState
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
  }

  override def receiveRecover: Receive = {
    case CharacterPlaced(hexCoordinates, character) =>
      placeCharacter(hexCoordinates, character)
      log.info(s"Recovered ${character.name} on $hexCoordinates")

    case CharacterMoved(hexCoordinates, character) =>
      moveCharacter(hexCoordinates, character)
      log.info(s"Recovered ${character.name} to $hexCoordinates")
  }

  override def receiveCommand: Receive = {
    case _ =>
  }
}