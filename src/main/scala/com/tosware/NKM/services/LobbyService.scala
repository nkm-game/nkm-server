package com.tosware.NKM.services

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import com.tosware.NKM.{DBManager, NKMTimeouts}
import com.tosware.NKM.actors._
import com.tosware.NKM.models.CommandResponse
import com.tosware.NKM.models.game._
import com.tosware.NKM.models.lobby._
import slick.jdbc.JdbcBackend
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.{Await, Future}

object LobbyService {
  sealed trait Event
  case class LobbyCreated(lobbyId: String) extends Event
  case object LobbyCreationFailure extends Event

  case object Success extends Event
  case object Failure extends Event
}

class LobbyService(implicit db: JdbcBackend.Database, system: ActorSystem, NKMDataService: NKMDataService) extends NKMTimeouts {
  import LobbyService._

  def createLobby(name: String, hostUserId: String): Event = {
    val randomId = java.util.UUID.randomUUID.toString
    val lobbyActor: ActorRef = system.actorOf(Lobby.props(randomId))
    Await.result(lobbyActor ? Lobby.Create(name, hostUserId), atMost) match {
      case CommandResponse.Success => LobbyCreated(randomId)
      case CommandResponse.Failure => LobbyCreationFailure
    }
  }

  def joinLobby(userId: String, request: LobbyJoinRequest): Event = {
    val gameActor: ActorRef = system.actorOf(Game.props(request.lobbyId))
    val gameState = Await.result(gameActor ? Game.GetState, atMost).asInstanceOf[GameState]
    if(gameState.gamePhase != GamePhase.NotStarted) return Failure

    val lobbyActor: ActorRef = system.actorOf(Lobby.props(request.lobbyId))

    Await.result(lobbyActor ? Lobby.UserJoin(userId), atMost) match {
      case CommandResponse.Success => Success
      case CommandResponse.Failure => Failure
    }
  }

  def leaveLobby(userId: String, request: LobbyLeaveRequest): Event = {
    val gameActor: ActorRef = system.actorOf(Game.props(request.lobbyId))
    val gameState = Await.result(gameActor ? Game.GetState, atMost).asInstanceOf[GameState]
    if(gameState.gamePhase != GamePhase.NotStarted) return Failure

    val lobbyActor: ActorRef = system.actorOf(Lobby.props(request.lobbyId))

    Await.result(lobbyActor ? Lobby.UserLeave(userId), atMost) match {
      case CommandResponse.Success => Success
      case CommandResponse.Failure => Failure
    }
  }

  def setHexmapName(username: String, request: SetHexMapNameRequest): Event = {
    val gameActor: ActorRef = system.actorOf(Game.props(request.lobbyId))
    val gameState = Await.result(gameActor ? Game.GetState, atMost).asInstanceOf[GameState]
    if(gameState.gamePhase != GamePhase.NotStarted) return Failure

    val lobbyActor: ActorRef = system.actorOf(Lobby.props(request.lobbyId))
    val nkmDataActor: ActorRef = system.actorOf(NKMData.props())

    val lobbyState = Await.result(lobbyActor ? Lobby.GetState, atMost).asInstanceOf[LobbyState]
    if(!lobbyState.hostUserId.contains(username)) return Failure

    val hexMaps = Await.result(nkmDataActor ? NKMData.GetHexMaps, atMost).asInstanceOf[List[HexMap]]
    if(!hexMaps.map(_.name).contains(request.hexMapName)) return Failure

    Await.result(lobbyActor ? Lobby.SetMapName(request.hexMapName), atMost) match {
      case CommandResponse.Success => Success
      case CommandResponse.Failure => Failure
    }
  }

  def setNumberOfCharactersPerPlayer(username: String, request: SetNumberOfCharactersPerPlayerRequest): Event = {
    if(request.charactersPerPlayer < 1) return Failure

    val gameActor: ActorRef = system.actorOf(Game.props(request.lobbyId))
    val gameState = Await.result(gameActor ? Game.GetState, atMost).asInstanceOf[GameState]
    if(gameState.gamePhase != GamePhase.NotStarted) return Failure

    val lobbyActor: ActorRef = system.actorOf(Lobby.props(request.lobbyId))

    val lobbyState = Await.result(lobbyActor ? Lobby.GetState, atMost).asInstanceOf[LobbyState]
    if(!lobbyState.hostUserId.contains(username)) return Failure

    Await.result(lobbyActor ? Lobby.SetNumberOfCharactersPerPlayer(request.charactersPerPlayer), atMost) match {
      case CommandResponse.Success => Success
      case CommandResponse.Failure => Failure
    }
  }

  def setNumberOfBans(username: String, request: SetNumberOfBansRequest): Event = {
    if(request.numberOfBans < 0) return Failure

    val gameActor: ActorRef = system.actorOf(Game.props(request.lobbyId))
    val gameState = Await.result(gameActor ? Game.GetState, atMost).asInstanceOf[GameState]
    if(gameState.gamePhase != GamePhase.NotStarted) return Failure

    val lobbyActor: ActorRef = system.actorOf(Lobby.props(request.lobbyId))

    val lobbyState = Await.result(lobbyActor ? Lobby.GetState, atMost).asInstanceOf[LobbyState]
    if(!lobbyState.hostUserId.contains(username)) return Failure

    Await.result(lobbyActor ? Lobby.SetNumberOfBans(request.numberOfBans), atMost) match {
      case CommandResponse.Success => Success
      case CommandResponse.Failure => Failure
    }
  }

  def setPickType(username: String, request: SetPickTypeRequest): Event = {
    val gameActor: ActorRef = system.actorOf(Game.props(request.lobbyId))
    val gameState = Await.result(gameActor ? Game.GetState, atMost).asInstanceOf[GameState]
    if(gameState.gamePhase != GamePhase.NotStarted) return Failure

    val lobbyActor: ActorRef = system.actorOf(Lobby.props(request.lobbyId))

    val lobbyState = Await.result(lobbyActor ? Lobby.GetState, atMost).asInstanceOf[LobbyState]
    if(!lobbyState.hostUserId.contains(username)) return Failure

    Await.result(lobbyActor ? Lobby.SetPickType(request.pickType), atMost) match {
      case CommandResponse.Success => Success
      case CommandResponse.Failure => Failure
    }
  }

  def setLobbyName(username: String, request: SetLobbyNameRequest): Event = {
    val gameActor: ActorRef = system.actorOf(Game.props(request.lobbyId))
    val gameState = Await.result(gameActor ? Game.GetState, atMost).asInstanceOf[GameState]
    if(gameState.gamePhase != GamePhase.NotStarted) return Failure

    val lobbyActor: ActorRef = system.actorOf(Lobby.props(request.lobbyId))

    val lobbyState = Await.result(lobbyActor ? Lobby.GetState, atMost).asInstanceOf[LobbyState]
    if(!lobbyState.hostUserId.contains(username)) return Failure

    Await.result(lobbyActor ? Lobby.SetLobbyName(request.newName), atMost) match {
      case CommandResponse.Success => Success
      case CommandResponse.Failure => Failure
    }
  }


  def startGame(username: String, request: StartGameRequest): Event = {
    val lobbyActor: ActorRef = system.actorOf(Lobby.props(request.lobbyId))
    val gameActor: ActorRef = system.actorOf(Game.props(request.lobbyId))

    val lobbyState = Await.result(lobbyActor ? Lobby.GetState, atMost).asInstanceOf[LobbyState]

    if(!lobbyState.hostUserId.contains(username)) return Failure
    if(lobbyState.chosenHexMapName.isEmpty) return Failure
    if(lobbyState.userIds.length < 2) return Failure

    val gameState = Await.result(gameActor ? Game.GetState, atMost).asInstanceOf[GameState]
    if(gameState.gamePhase != GamePhase.NotStarted) return Failure

    Await.result(lobbyActor ? Lobby.StartGame, atMost) match {
      case CommandResponse.Success => Success
      case CommandResponse.Failure => Failure
    }

  }

  def getAllLobbies(): Future[Seq[LobbyState]] = {
    val lobbiesAction = DBManager.lobbies.result
    db.run(lobbiesAction)
  }

  def getLobby(lobbyId: String): Future[LobbyState] = {
    val lobbyAction = DBManager.lobbies.filter(_.id === lobbyId).result.head
    db.run(lobbyAction)
  }
}
