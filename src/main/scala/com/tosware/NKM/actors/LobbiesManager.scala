package com.tosware.NKM.actors

import akka.actor.{ActorLogging, ActorRef, Props}
import akka.event.LoggingAdapter
import akka.persistence.{PersistentActor, RecoveryCompleted}
import com.tosware.NKM.NKMTimeouts
import com.tosware.NKM.actors.LobbiesManager.Event.LobbyCreated
import com.tosware.NKM.actors.LobbiesManager.GetLobbyActorResponse
import com.tosware.NKM.actors.LobbiesManager.Query.GetLobbyActor
import com.tosware.NKM.models.CommandResponse._
import com.tosware.NKM.services.NKMDataService

object LobbiesManager {

  object Query {
    sealed trait Query
    case class GetLobbyActor(lobbyId: String) extends Query
  }

  case class GetLobbyActorResponse(lobbyActor: Option[ActorRef])

  sealed trait Event
  object Event {
    case class LobbyCreated(lobbyId: String) extends Event
  }

  def props(NKMDataService: NKMDataService): Props =
    Props(new LobbiesManager(NKMDataService))
}

class LobbiesManager(NKMDataService: NKMDataService)
  extends PersistentActor
    with ActorLogging
    with NKMTimeouts {

  override def persistenceId: String = s"lobbies-manager"

  override def log: LoggingAdapter = akka.event.Logging(context.system, s"${this.getClass}($persistenceId)")

  var lobbies: Map[String, ActorRef] = Map()

  def setLobby(lobbyId: String, lobbyActor: ActorRef): Unit =
    lobbies = lobbies.updated(lobbyId, lobbyActor)

  def persistAndPublish[A](event: A)(handler: A => Unit): Unit = {
    context.system.eventStream.publish(event)
    persist(event)(handler)
  }

  override def receive: Receive = {
    case GetLobbyActor(lobbyId) =>
      sender() ! GetLobbyActorResponse(lobbies.get(lobbyId))
    case createCommand @ Lobby.Create(_, _) =>
      val randomId = java.util.UUID.randomUUID.toString
      val lobbyActor: ActorRef = context.actorOf(Lobby.props(randomId)(NKMDataService))

      persistAndPublish(LobbyCreated(randomId)) { _ =>
        lobbyActor ! createCommand
        sender() ! Success(randomId)
        setLobby(randomId, lobbyActor)
      }
    case e => log.warning(s"Unknown message: $e")
  }

  override def receiveRecover: Receive = {
    case LobbyCreated(lobbyId) =>
      val lobbyActor = context.actorOf(Lobby.props(lobbyId)(NKMDataService))
      setLobby(lobbyId, lobbyActor)
      log.debug(s"Recovered create of $lobbyId")
    case RecoveryCompleted =>
    case Success(_) =>
    case e => log.warning(s"Unknown message: $e")
  }

  override def receiveCommand: Receive = {
    case _ =>
  }
}