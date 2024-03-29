package com.tosware.nkm.actors

import akka.actor.{ActorRef, Props}
import akka.persistence.{PersistentActor, RecoveryCompleted}
import com.tosware.nkm.actors.GameIdTrackerActor.*
import com.tosware.nkm.actors.GameIdTrackerActor.Event.*
import com.tosware.nkm.models.CommandResponse.*
import com.tosware.nkm.services.{NkmDataService, UserService}
import com.tosware.nkm.{GameId, Logging, NkmTimeouts}

object GameIdTrackerActor {
  sealed trait Query
  object Query {
    case class GetGameActor(gameId: GameId) extends Query
    case class GetLobbyActor(gameId: GameId) extends Query
  }

  sealed trait Response
  object Response {

    case class GetGameActorResponse(gameActor: ActorRef) extends Response
    case class GetLobbyActorResponse(lobbyActor: ActorRef) extends Response
    case object GameIdDoesNotExist extends Response
  }

  sealed trait Event
  object Event {
    case class GameIdTracked(gameId: GameId) extends Event
  }

  def props(nkmDataService: NkmDataService, userService: UserService): Props =
    Props(new GameIdTrackerActor(nkmDataService, userService))
}

class GameIdTrackerActor(nkmDataService: NkmDataService, userService: UserService)
    extends PersistentActor
    with Logging
    with NkmTimeouts {

  override def persistenceId: String = s"game-id-tracker"

  override def preStart(): Unit = log.info("Games id tracker started")

  var trackedGameIds: Seq[GameId] = Seq()

  object Session {
    private var gameActors: Map[String, ActorRef] = Map()
    private var lobbyActors: Map[String, ActorRef] = Map()

    def getGameActor(gameId: GameId): ActorRef =
      gameActors.getOrElse(
        gameId, {
          val gameActor: ActorRef = context.actorOf(Game.props(gameId)(nkmDataService))
          gameActors = gameActors.updated(gameId, gameActor)
          return gameActor
        },
      )

    def getLobbyActor(gameId: GameId): ActorRef =
      lobbyActors.getOrElse(
        gameId, {
          val lobbyActor: ActorRef = context.actorOf(Lobby.props(gameId)(nkmDataService, userService))
          lobbyActors = lobbyActors.updated(gameId, lobbyActor)
          return lobbyActor
        },
      )
  }

  def trackGameId(id: GameId): Unit =
    trackedGameIds = id +: trackedGameIds

  def persistAndPublish[A](event: A)(handler: A => Unit): Unit = {
    context.system.eventStream.publish(event)
    persist(event)(handler)
    log.warn(event.toString)
  }

  override def receive: Receive = {
    case Query.GetGameActor(gameId) =>
      val response: Response = if (trackedGameIds.contains(gameId)) {
        Response.GetGameActorResponse(Session.getGameActor(gameId))
      } else {
        Response.GameIdDoesNotExist
      }
      sender() ! response
    case Query.GetLobbyActor(gameId) =>
      val response: Response = if (trackedGameIds.contains(gameId)) {
        Response.GetLobbyActorResponse(Session.getLobbyActor(gameId))
      } else {
        Response.GameIdDoesNotExist
      }
      sender() ! response
    case createCommand @ Lobby.Create(_, _) =>
      val randomId = java.util.UUID.randomUUID.toString
      val lobbyActor = Session.getLobbyActor(randomId)

      persistAndPublish(GameIdTracked(randomId)) { _ =>
        lobbyActor ! createCommand
        trackGameId(randomId)
        sender() ! Success(randomId)
      }
    case e => log.warn(s"Unknown message: $e")
  }

  override def receiveRecover: Receive = {
    case GameIdTracked(gameId) =>
      trackGameId(gameId)
      log.debug(s"Recovered track of $gameId")
    case RecoveryCompleted =>
    case e                 => log.warn(s"Unknown message: $e")
  }

  override def receiveCommand: Receive = {
    case _ =>
  }
}
