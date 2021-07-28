package com.tosware.NKM.services

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import com.tosware.NKM.{DBManager, NKMTimeouts}
import com.tosware.NKM.actors.Lobby
import com.tosware.NKM.models.lobby.{LobbyJoinRequest, LobbyLeaveRequest, LobbyState}
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
      case Lobby.CreateSuccess => LobbyCreated(randomId)
      case Lobby.CreateFailure => LobbyCreationFailure
    }
  }

  def joinLobby(userId: String, request: LobbyJoinRequest)(implicit system: ActorSystem): Event = {
    val lobbyActor: ActorRef = system.actorOf(Lobby.props(request.lobbyId))
    Await.result(lobbyActor ? Lobby.UserJoin(userId), atMost) match {
      case Lobby.JoinSuccess => Success
      case Lobby.JoinFailure => Failure
    }
  }

  def leaveLobby(userId: String, request: LobbyLeaveRequest)(implicit system: ActorSystem): Event = {
    val lobbyActor: ActorRef = system.actorOf(Lobby.props(request.lobbyId))
    Await.result(lobbyActor ? Lobby.UserLeave(userId), atMost) match {
      case Lobby.LeaveSuccess => Success
      case Lobby.LeaveFailure => Failure
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
    println(lobby)
    lobby
  }
}
