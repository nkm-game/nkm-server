package com.tosware.NKM.services

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import com.tosware.NKM.{DBManager, NKMTimeouts}
import com.tosware.NKM.actors.{Game, Lobby, NKMData}
import com.tosware.NKM.models.{GameState, HexMap}
import com.tosware.NKM.models.lobby.{LobbyJoinRequest, LobbyLeaveRequest, LobbyState, SetHexmapNameRequest, StartGameRequest}
import slick.jdbc.JdbcBackend
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.Await

object LobbyService {
  sealed trait Event
  case class LobbyCreated(lobbyId: String) extends Event
  case object LobbyCreationFailure extends Event

  case object Success extends Event
  case object Failure extends Event
}

class LobbyService(implicit db: JdbcBackend.Database) extends NKMTimeouts {
  import LobbyService._

  def createLobby(name: String, hostUserId: String)(implicit system: ActorSystem): Event = {
    val randomId = java.util.UUID.randomUUID.toString
    val lobbyActor: ActorRef = system.actorOf(Lobby.props(randomId))
    Await.result(lobbyActor ? Lobby.Create(name, hostUserId), atMost) match {
      case Lobby.Success => LobbyCreated(randomId)
      case Lobby.Failure => LobbyCreationFailure
    }
  }

  def joinLobby(userId: String, request: LobbyJoinRequest)(implicit system: ActorSystem): Event = {
    val lobbyActor: ActorRef = system.actorOf(Lobby.props(request.lobbyId))
    Await.result(lobbyActor ? Lobby.UserJoin(userId), atMost) match {
      case Lobby.Success => Success
      case Lobby.Failure => Failure
    }
  }

  def leaveLobby(userId: String, request: LobbyLeaveRequest)(implicit system: ActorSystem): Event = {
    val lobbyActor: ActorRef = system.actorOf(Lobby.props(request.lobbyId))
    Await.result(lobbyActor ? Lobby.UserLeave(userId), atMost) match {
      case Lobby.Success => Success
      case Lobby.Failure => Failure
    }
  }

  def setHexmapName(username: String, request: SetHexmapNameRequest)(implicit system: ActorSystem): Event = {
    val lobbyActor: ActorRef = system.actorOf(Lobby.props(request.lobbyId))
    val nkmDataActor: ActorRef = system.actorOf(NKMData.props())

    val lobbyState = Await.result(lobbyActor ? Lobby.GetState, atMost).asInstanceOf[LobbyState]
    if(lobbyState.hostUserId.getOrElse() != username) return Failure

    val hexMaps = Await.result(nkmDataActor ? NKMData.GetHexMaps, atMost).asInstanceOf[List[HexMap]]
    if(!hexMaps.map(_.name).contains(request.hexMapName)) return Failure

    Await.result(lobbyActor ? Lobby.SetMapName(request.hexMapName), atMost) match {
      case Lobby.Success => Success
      case Lobby.Failure => Failure
    }
  }

  def startGame(username: String, request: StartGameRequest)(implicit system: ActorSystem): Event = {
    val lobbyActor: ActorRef = system.actorOf(Lobby.props(request.lobbyId))
    val gameActor: ActorRef = system.actorOf(Game.props(request.lobbyId))

    val lobbyState = Await.result(lobbyActor ? Lobby.GetState, atMost).asInstanceOf[LobbyState]

    if(lobbyState.hostUserId.getOrElse() != username) return Failure
    if(lobbyState.chosenHexMapName.isEmpty) return Failure
    if(lobbyState.userIds.length < 2) return Failure

    val gameState = Await.result(gameActor ? Game.GetState, atMost).asInstanceOf[GameState]
    if(gameState.isStarted) return Failure

    Await.result(gameActor ? Game.StartGame, atMost) match {
      case Game.Success => Success
      case Game.Failure => Failure
    }

  }



  def getAllLobbies(): Seq[LobbyState] = {
    val lobbysAction = DBManager.lobbies.result
    val lobbys = Await.result(db.run(lobbysAction), atMost)
    lobbys
  }

  def getLobby(lobbyId: String): LobbyState = {
    val lobbyAction = DBManager.lobbies.filter(_.id === lobbyId).result.head
    val lobby = Await.result(db.run(lobbyAction), atMost)
    lobby
  }
}
