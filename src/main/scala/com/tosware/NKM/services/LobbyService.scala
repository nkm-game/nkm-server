package com.tosware.NKM.services

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import com.tosware.NKM.{DBManager, NKMTimeouts}
import com.tosware.NKM.actors.Lobby
import com.tosware.NKM.models.LobbyState
import slick.jdbc.JdbcBackend
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.Await

object LobbyService {
  sealed trait Event
  case class LobbyCreated(lobbyId: String) extends Event
  case object LobbyCreationFailure extends Event
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

  def getAllLobbies(): Seq[LobbyState] = {
    val lobbysAction = DBManager.lobbies.result
    val lobbys = Await.result(db.run(lobbysAction), atMost)
    lobbys
  }
}
