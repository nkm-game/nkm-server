package com.tosware.nkm.actors

import akka.actor.{ActorLogging, ActorRef, Props}
import akka.event.LoggingAdapter
import akka.persistence.{PersistentActor, RecoveryCompleted}
import com.tosware.nkm.NkmTimeouts
import com.tosware.nkm.actors.GamesManager.Event.GameTracked
import com.tosware.nkm.actors.GamesManager.GetGameActorResponse
import com.tosware.nkm.actors.GamesManager.Query.GetGameActor
import com.tosware.nkm.services.NkmDataService

object GamesManager {
  object Query {
    sealed trait Query
    case class GetGameActor(lobbyId: String) extends Query
  }

  case class GetGameActorResponse(gameActor: ActorRef)

  sealed trait Event
  object Event {
    case class GameTracked(lobbyId: String) extends Event
  }

  def props(nkmDataService: NkmDataService): Props = Props(new GamesManager(nkmDataService))
}

class GamesManager(nkmDataService: NkmDataService)
    extends PersistentActor
    with ActorLogging
    with NkmTimeouts {

  override def persistenceId: String = s"games-manager"

  override def log: LoggingAdapter = akka.event.Logging(context.system, s"${this.getClass}($persistenceId)")

  override def preStart(): Unit = log.info("Games manager started")

  var games: Map[String, ActorRef] = Map()

  def setGame(lobbyId: String): Unit = {
    val gameActor: ActorRef = context.actorOf(Game.props(lobbyId)(nkmDataService))
    games = games.updated(lobbyId, gameActor)
  }

  def persistAndPublish[A](event: A)(handler: A => Unit): Unit = {
    context.system.eventStream.publish(event)
    persist(event)(handler)
    log.warning(event.toString)
  }

  override def receive: Receive = {
    case GetGameActor(lobbyId) =>
      if (games.isDefinedAt(lobbyId)) {
        sender() ! GetGameActorResponse(games(lobbyId))
      } else {
        persistAndPublish(GameTracked(lobbyId)) { _ =>
          setGame(lobbyId)
          sender() ! GetGameActorResponse(games(lobbyId))
        }
      }
    case e => log.warning(s"Unknown message: $e")
  }

  override def receiveRecover: Receive = {
    case GameTracked(lobbyId) =>
      setGame(lobbyId)
      log.debug(s"Recovered create of $lobbyId")
    case RecoveryCompleted =>
    case e                 => log.warning(s"Unknown message: $e")
  }

  override def receiveCommand: Receive = {
    case _ =>
  }
}
