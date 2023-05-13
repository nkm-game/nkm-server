package com.tosware.nkm.actors

import akka.actor.{ActorLogging, ActorRef, Props}
import akka.event.LoggingAdapter
import akka.persistence.{PersistentActor, RecoveryCompleted}
import com.tosware.nkm.NkmTimeouts
import com.tosware.nkm.actors.LobbiesManager.Event.LobbyCreated
import com.tosware.nkm.actors.LobbiesManager.GetLobbyActorResponse
import com.tosware.nkm.actors.LobbiesManager.Query.GetLobbyActor
import com.tosware.nkm.models.CommandResponse.*
import com.tosware.nkm.services.NkmDataService

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

  def props(nkmDataService: NkmDataService): Props =
    Props(new LobbiesManager(nkmDataService))
}

class LobbiesManager(nkmDataService: NkmDataService)
  extends PersistentActor
    with ActorLogging
    with NkmTimeouts {

  override def persistenceId: String = s"lobbies-manager"

  override def log: LoggingAdapter = akka.event.Logging(context.system, s"${this.getClass}($persistenceId)")

  override def preStart(): Unit = log.info("Lobbies manager started")

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
      val lobbyActor: ActorRef = context.actorOf(Lobby.props(randomId)(nkmDataService))

      persistAndPublish(LobbyCreated(randomId)) { _ =>
        lobbyActor ! createCommand
        sender() ! Success(randomId)
        setLobby(randomId, lobbyActor)
      }
    case e => log.warning(s"Unknown message: $e")
  }

  override def receiveRecover: Receive = {
    case LobbyCreated(lobbyId) =>
      val lobbyActor = context.actorOf(Lobby.props(lobbyId)(nkmDataService))
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