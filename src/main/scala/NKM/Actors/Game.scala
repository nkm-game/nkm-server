package NKM.Actors

import com.softwaremill.quicklens._
import NKM._
import akka.actor.{Actor, ActorLogging, Props}

object Game {
  sealed trait Commands
  case object GetState extends Commands
  case class PlaceCharacter(hexCoordinates: HexCoordinates, character: NKMCharacter) extends Commands

  def props(id: String): Props = Props(new Game(id, HexMap("test", Set[HexCell](HexCell(HexCoordinates(4, 5), Normal, None, Set(), None)))))
}

class Game(id: String, hexMap: HexMap) extends Actor with ActorLogging {
  import Game._
  val charactersOutsideMap: Set[NKMCharacter] = Set[NKMCharacter](
    NKMCharacter("Aqua", 12, Stat(32), Stat(43), Stat(4), Stat(34), Stat(4)),
    NKMCharacter("Dekomori Sanae", 14, Stat(32), Stat(43), Stat(4), Stat(34), Stat(4)),
    NKMCharacter("Aqua", 0, Stat(34), Stat(43), Stat(4), Stat(34), Stat(5))
  )
  var gameState: GameState = GameState(hexMap, charactersOutsideMap)

  def placeCharacter(cellCoordinates: HexCoordinates, character: NKMCharacter): Unit =
//    gameState = gameState.modify(_.hexMap.cells.eachWhere(_.coordinates.equals(cellCoordinates)).character).setTo(Some(character))
      gameState = gameState.modify(_.hexMap.cells.each).using {
        case cell if cell.coordinates == cellCoordinates => HexCell(cell.coordinates, cell.cellType, Some(character), cell.effects, cell.spawnNumber)
        case cell => cell
      }.modify(_.charactersOutsideMap).using(_.filter(_ != character))

//  override def persistenceId: String = "game-$id"
  override def receive: Receive = {
    case GetState =>
      log.info("Received state request")
      sender ! gameState
    case PlaceCharacter(hexCoordinates, character) =>
      log.info(s"Placing ${character.name} on $hexCoordinates")
      placeCharacter(hexCoordinates, character)
  }

//  override def receiveRecover: Receive = {
//    case _ =>
//  }
//
//  override def receiveCommand: Receive = {
//    case _ =>
//  }
}