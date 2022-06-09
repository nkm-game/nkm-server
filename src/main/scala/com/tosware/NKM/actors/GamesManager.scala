package com.tosware.NKM.actors

import akka.actor.{ActorLogging, ActorRef, Props, Terminated}
import akka.event.LoggingAdapter
import akka.persistence.{PersistentActor, RecoveryCompleted}
import com.tosware.NKM.NKMTimeouts
import com.tosware.NKM.actors.GamesManager.Event.GameTracked
import com.tosware.NKM.actors.GamesManager.GetGameActorResponse
import com.tosware.NKM.actors.GamesManager.Query.GetGameActor
import com.tosware.NKM.services.NKMDataService

object GamesManager {
  object Query {
    sealed trait Query
    case class GetGameActor(gameId: String) extends Query
  }

  case class GetGameActorResponse(gameActor: ActorRef)

  sealed trait Event
  object Event {
    case class GameTracked(gameId: String) extends Event
  }

  def props(NKMDataService: NKMDataService): Props = Props(new GamesManager(NKMDataService))
}

class GamesManager(NKMDataService: NKMDataService)
  extends PersistentActor
    with ActorLogging
    with NKMTimeouts {

  override def persistenceId: String = s"games-manager"

  override def log: LoggingAdapter = akka.event.Logging(context.system, s"${this.getClass}($persistenceId)")

  override def preStart(): Unit = log.info("Games manager started")

  var games: Map[String, ActorRef] = Map()

  def setGame(gameId: String): Unit = {
    val gameActor: ActorRef = context.actorOf(Game.props(gameId)(NKMDataService))
    games = games.updated(gameId, gameActor)
  }

  def persistAndPublish[A](event: A)(handler: A => Unit): Unit = {
    context.system.eventStream.publish(event)
    persist(event)(handler)
    log.warning(event.toString)
  }

  override def receive: Receive = {
    case GetGameActor(gameId) =>
      if (games.isDefinedAt(gameId)) {
        sender() ! GetGameActorResponse(games(gameId))
      } else {
        persistAndPublish(GameTracked(gameId)) { _ =>
          setGame(gameId)
          sender() ! GetGameActorResponse(games(gameId))
        }
      }
    case e => log.warning(s"Unknown message: $e")
  }

  override def receiveRecover: Receive = {
    case GameTracked(gameId) =>
      setGame(gameId)
      log.debug(s"Recovered create of $gameId")
    case RecoveryCompleted =>
    case e => log.warning(s"Unknown message: $e")
  }

  override def receiveCommand: Receive = {
    case _ =>
  }
}