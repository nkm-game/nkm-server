package com.tosware.NKM.actors

import akka.actor.{ActorLogging, Props}
import akka.persistence.{PersistentActor, RecoveryCompleted}
import com.softwaremill.quicklens._
import com.tosware.NKM.models.CommandResponse._
import com.tosware.NKM.models.game.GamePhase._
import com.tosware.NKM.models.game.PickType.AllRandom
import com.tosware.NKM.models.game._
import com.tosware.NKM.services.NKMDataService

import scala.util.Random

object Game {
  sealed trait Query
  case object GetState extends Query

  sealed trait Command
  case class StartGame(gameStartDependencies: GameStartDependencies) extends Command
  case class Surrender(playerName: String) extends Command
//  case class SetPlayers(names: List[String]) extends Command
//  case class AddCharacter(playerName: String, character: NKMCharacter) extends Command
  case class PlaceCharacter(hexCoordinates: HexCoordinates, characterId: String) extends Command
  case class MoveCharacter(hexCoordinates: HexCoordinates, characterId: String) extends Command

  sealed trait Event {
    val id: String
  }
  case class GameStarted(id: String, gameStartDependencies: GameStartDependencies) extends Event
  case class Surrendered(id: String, playerName: String) extends Event
//  case class PlayersSet(names: List[String]) extends Event
//  case class CharacterAdded(playerName: String, character: NKMCharacter) extends Event
  case class CharacterPlaced(id: String, hexCoordinates: HexCoordinates, characterId: String) extends Event
  case class CharacterMoved(id: String, hexCoordinates: HexCoordinates, characterId: String) extends Event

  def props(id: String)(implicit NKMDataService: NKMDataService): Props = Props(new Game(id))
}

class Game(id: String)(implicit NKMDataService: NKMDataService) extends PersistentActor with ActorLogging {
  import Game._
  var gameState: GameState = GameState.empty(id)
  val random: Random = new Random(id.hashCode)

  def startGame(g: GameStartDependencies): Unit = {
    gameState = gameState.copy(
      players = g.players,
      hexMap = Some(g.hexMap),
      pickType = g.pickType,
      numberOfBans = g.numberOfBans,
      numberOfCharactersPerPlayers = g.numberOfCharactersPerPlayers,
      gamePhase = GamePhase.CharacterPick,
    )

    if(gameState.pickType == AllRandom) {
      val pickedCharacters = random.shuffle(NKMDataService.getCharactersMetadata).grouped(gameState.numberOfCharactersPerPlayers).take(gameState.players.length)
      val playersWithAssignedCharacters = gameState.players.zip(pickedCharacters).map(x => {
        val (player, characters) = x
        player.copy(characters = characters.map(c => NKMCharacter.fromMetadata(java.util.UUID.nameUUIDFromBytes(random.nextBytes(16)).toString, c)).toList)
      })
      gameState = gameState.copy(gamePhase = GamePhase.CharacterPlacing, players = playersWithAssignedCharacters, characterIdsOutsideMap = playersWithAssignedCharacters.flatMap(c => c.characters.map(c => c.id)))
    }
  }

  def surrender(playerName: String): Unit = {
    def filterPlayer: Player => Boolean = _.name == playerName
    gameState = gameState.modify(_.players.eachWhere(filterPlayer).victoryStatus).setTo(VictoryStatus.Lost)
  }

  def placeCharacter(targetCellCoordinates: HexCoordinates, characterId: String): Unit =
      gameState = gameState.modify(_.hexMap.each.cells.each).using {
        case cell if cell.coordinates == targetCellCoordinates => HexCell(cell.coordinates, cell.cellType, Some(characterId), cell.effects, cell.spawnNumber)
        case cell => cell
      }.modify(_.characterIdsOutsideMap).using(_.filter(_ != characterId))
        .modify(_.turn).using(oldTurn => Turn(oldTurn.number + 1))

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
      if(gameState.gamePhase != NotStarted) {
        sender() ! Failure
      } else {
        val e = GameStarted(id, gameStartDependencies)
        persistAndPublish(e) { _ =>
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
    case Surrender(playerName) =>
      log.info(s"Surrendering $playerName")
      //TODO: check if game is started
      val playerOption = gameState.players.find(_.name == playerName)
      if(playerOption.isEmpty) {
        sender() ! Failure //TODO ("This player is not in this game.")
      } else {
        val player = playerOption.get
        if(player.victoryStatus != VictoryStatus.Pending) {
          sender() ! Failure //TODO ("This player already finished the game."))
        } else {
          val e = Surrendered(id, playerName)
          persistAndPublish(e) { _ =>
            surrender(playerName)
            log.info(s"Surrendered $playerName")
            sender() ! Success
          }
        }
      }
    case PlaceCharacter(hexCoordinates, characterId) =>
      log.info(s"Placing $characterId on $hexCoordinates")
      val e = CharacterPlaced(id, hexCoordinates, characterId)
      persistAndPublish(e) { _ =>
        placeCharacter(hexCoordinates, characterId)
        log.info(s"Persisted $characterId on $hexCoordinates")
        sender() ! Success
      }
    case MoveCharacter(hexCoordinates, characterId) =>
      log.info(s"Moving $characterId to $hexCoordinates")
      val e = CharacterMoved(id, hexCoordinates, characterId)
      persistAndPublish(e) { _ =>
        moveCharacter(hexCoordinates, characterId)
        log.info(s"Persisted $characterId on $hexCoordinates")
        sender() ! Success
      }
    case e => log.warning(s"Unknown message: $e")
  }

  override def receiveRecover: Receive = {
    case GameStarted(_, gameStartDependencies) =>
      startGame(gameStartDependencies)
      log.debug(s"Recovered game start")
    case Surrendered(_, playerName) =>
      surrender(playerName)
      log.debug(s"Recovered $playerName surrender")
//    case PlayersSet(names) =>
//      setPlayers(names)
//      log.debug(s"Recovered players: $names")
//    case CharacterAdded(player, character) =>
//      addCharacter(player, character)
//      log.debug(s"Recovered character: ${character.name}")
    case CharacterPlaced(_, hexCoordinates, characterId) =>
      placeCharacter(hexCoordinates, characterId)
      log.debug(s"Recovered $characterId on $hexCoordinates")
    case CharacterMoved(_, hexCoordinates, characterId) =>
      moveCharacter(hexCoordinates, characterId)
      log.debug(s"Recovered $characterId to $hexCoordinates")
    case RecoveryCompleted =>
    case e => log.warning(s"Unknown message: $e")
  }

  override def receiveCommand: Receive = {
    case _ =>
  }
}