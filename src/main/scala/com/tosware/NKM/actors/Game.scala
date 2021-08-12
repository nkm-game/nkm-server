package com.tosware.NKM.actors

import akka.actor.{ActorLogging, Props}
import akka.persistence.{PersistentActor, RecoveryCompleted}
import com.softwaremill.quicklens._
import com.tosware.NKM.models.CommandResponse._
import com.tosware.NKM.models.game.GamePhase._
import com.tosware.NKM.models.game._

object Game {
  sealed trait Query
  case object GetState extends Query

  sealed trait Command
  case class StartGame(gameStartDependencies: GameStartDependencies) extends Command
//  case class SetPlayers(names: List[String]) extends Command
//  case class AddCharacter(playerName: String, character: NKMCharacter) extends Command
  case class PlaceCharacter(hexCoordinates: HexCoordinates, characterId: String) extends Command
  case class MoveCharacter(hexCoordinates: HexCoordinates, characterId: String) extends Command

  sealed trait Event
  case class GameStarted(gameId: String, gameStartDependencies: GameStartDependencies) extends Event
//  case class PlayersSet(names: List[String]) extends Event
//  case class CharacterAdded(playerName: String, character: NKMCharacter) extends Event
  case class CharacterPlaced(hexCoordinates: HexCoordinates, characterId: String) extends Event
  case class CharacterMoved(hexCoordinates: HexCoordinates, characterId: String) extends Event

  def props(id: String): Props = Props(new Game(id))
}

class Game(id: String) extends PersistentActor with ActorLogging {
  import Game._
  var gameState: GameState = GameState.empty

  def startGame(g: GameStartDependencies): Unit =
    gameState = gameState.copy(
      players = g.players,
      hexMap = Some(g.hexMap),
      pickType = g.pickType,
      numberOfBans = g.numberOfBans,
      numberOfCharactersPerPlayers = g.numberOfCharactersPerPlayers,
      gamePhase = CharacterPick,
    )

  def placeCharacter(targetCellCoordinates: HexCoordinates, characterId: String): Unit =
      gameState = gameState.modify(_.hexMap.each.cells.each).using {
        case cell if cell.coordinates == targetCellCoordinates => HexCell(cell.coordinates, cell.cellType, Some(characterId), cell.effects, cell.spawnNumber)
        case cell => cell
      }.modify(_.characterIdsOutsideMap).using(_.filter(_ != characterId))

  def moveCharacter(parentCellCoordinates: HexCoordinates, characterId: String): Unit = {
    val parentCell = gameState.hexMap.get.cells.find(_.characterId.contains(characterId)).getOrElse {
      log.error(s"Unable to move character $characterId to $parentCellCoordinates")
      return
    }
    gameState = gameState.modify(_.hexMap.each.cells.each).using {
      case cell if cell == parentCell => HexCell(cell.coordinates, cell.cellType, None, cell.effects, cell.spawnNumber)
      case cell if cell.coordinates == parentCellCoordinates => HexCell(cell.coordinates, cell.cellType, Some(characterId), cell.effects, cell.spawnNumber)
      case cell => cell
    }
  }

//  def setPlayers(names: List[String]): Unit =
//    gameState = gameState.modify(_.players).setTo(names.map(n => Player(n)))

//  def addCharacter(playerName: String, character: NKMCharacter): Unit = {
//    val currentCharacters = gameState.players.find(_.name == playerName).getOrElse {
//      log.error(s"Player $playerName not found")
//      return
//    }.characters
//    gameState = gameState.modify(_.players.each).using {
//      case p if p.name == playerName => p.modify(_.characters).setTo(character :: currentCharacters)
//      case p => p
//    }.modify(_.characterIdsOutsideMap).setTo(character.id :: gameState.characterIdsOutsideMap)
//  }

  def setMap(hexMap: HexMap): Unit =
    gameState = gameState.copy(hexMap = Some(hexMap))

  override def persistenceId: String = s"game-$id"
  override def receive: Receive = {
    case GetState =>
      log.info("Received state request")
      sender() ! gameState
    case StartGame(gameStartDependencies) =>
      log.info(s"Starting the game")
      if(gameState.gamePhase != NotStarted) {
        sender() ! Failure
      } else {
        persist(GameStarted(id, gameStartDependencies)) { _ =>
          startGame(gameStartDependencies)
          sender() ! Success
        }
      }
//    case SetPlayers(names) =>
//      log.info(s"Set player event: $names")
//      persist(PlayersSet(names)) { _ =>
//        setPlayers(names)
//        log.info(s"Persisted players: $names")
//      }
//    case AddCharacter(player, character) =>
//      log.info(s"Add character event: ${character.name}")
//      persist(CharacterAdded(player, character)) { _ =>
//        addCharacter(player, character)
//        log.info(s"Persisted character: ${character.name}")
//      }
    case PlaceCharacter(hexCoordinates, characterId) =>
      log.info(s"Placing $characterId on $hexCoordinates")
      persist(CharacterPlaced(hexCoordinates, characterId)) { _ =>
        placeCharacter(hexCoordinates, characterId)
        log.info(s"Persisted $characterId on $hexCoordinates")
      }
    case MoveCharacter(hexCoordinates, characterId) =>
      log.info(s"Moving $characterId to $hexCoordinates")
      persist(CharacterMoved(hexCoordinates, characterId)) { _ =>
        moveCharacter(hexCoordinates, characterId)
        log.info(s"Persisted $characterId on $hexCoordinates")
      }
    case e => log.warning(s"Unknown message: $e")
  }

  override def receiveRecover: Receive = {
    case GameStarted(_, gameStartDependencies) =>
      startGame(gameStartDependencies)
      log.debug(s"Recovered game start")
//    case PlayersSet(names) =>
//      setPlayers(names)
//      log.debug(s"Recovered players: $names")
//    case CharacterAdded(player, character) =>
//      addCharacter(player, character)
//      log.debug(s"Recovered character: ${character.name}")
    case CharacterPlaced(hexCoordinates, characterId) =>
      placeCharacter(hexCoordinates, characterId)
      log.debug(s"Recovered $characterId on $hexCoordinates")
    case CharacterMoved(hexCoordinates, characterId) =>
      moveCharacter(hexCoordinates, characterId)
      log.debug(s"Recovered $characterId to $hexCoordinates")
    case RecoveryCompleted =>
    case e => log.warning(s"Unknown message: $e")
  }

  override def receiveCommand: Receive = {
    case _ =>
  }
}