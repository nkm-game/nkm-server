package com.tosware.NKM.actors

import akka.actor.{ActorLogging, ActorRef, Props}
import akka.event.LoggingAdapter
import akka.pattern.ask
import akka.persistence.{PersistentActor, RecoveryCompleted}
import com.tosware.NKM.NKMTimeouts
import com.tosware.NKM.actors.LobbiesManager.Event.LobbyCreated
import com.tosware.NKM.actors.LobbiesManager.GetLobbyActorResponse
import com.tosware.NKM.actors.LobbiesManager.Query.GetLobbyActor
import com.tosware.NKM.models.CommandResponse._
import com.tosware.NKM.services.NKMDataService

import scala.concurrent.Await

object LobbiesManager {

  object Query {
    sealed trait Query
    case class GetLobbyActor(lobbyId: String)
  }

  case class GetLobbyActorResponse(lobbyActor: Option[ActorRef])

  sealed trait Event
  object Event {
    case class LobbyCreated(lobbyId: String) extends Event
  }

  def props(NKMDataService: NKMDataService): Props = Props(new LobbiesManager(NKMDataService))
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
      Await.result(lobbyActor ? createCommand, atMost) match {
        case Success(_) =>
          persistAndPublish(LobbyCreated(randomId)) { _ =>
            setLobby(randomId, lobbyActor)
            sender() ! Success(randomId)
          }
        case Failure(msg) => Failure(msg)
      }
    case e => log.warning(s"Unknown message: $e")
  }

  override def receiveRecover: Receive = {
    case LobbyCreated(lobbyId) =>
      val lobbyActor = context.actorOf(Lobby.props(lobbyId)(NKMDataService))
      setLobby(lobbyId, lobbyActor)
      log.debug(s"Recovered create of $lobbyId")
    case RecoveryCompleted =>
    case e => log.warning(s"Unknown message: $e")
  }

  override def receiveCommand: Receive = {
    case _ =>
  }
}