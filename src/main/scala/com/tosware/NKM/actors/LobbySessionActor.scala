package com.tosware.NKM.actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Terminated}
import akka.http.scaladsl.model.StatusCodes
import com.tosware.NKM.NKMTimeouts
import com.tosware.NKM.actors.WebsocketUser.OutgoingMessage
import com.tosware.NKM.models.lobby.{LobbyResponseType, WebsocketLobbyResponse}
import com.tosware.NKM.serializers.NKMJsonProtocol
import com.tosware.NKM.services.LobbyService
import spray.json._

import scala.collection.mutable
import scala.concurrent.Await

object LobbySessionActor {
  case class Observe(lobbyId: String, websocketUserOutput: ActorRef)

  def props()(implicit lobbyService: LobbyService): Props = Props(new LobbySessionActor)
}

class LobbySessionActor(implicit val lobbyService: LobbyService)
  extends Actor
  with ActorLogging
  with NKMTimeouts
  with NKMJsonProtocol
{
  import LobbySessionActor._

  override def preStart(): Unit = {
    log.info("LobbySessionActor started")
    context.system.eventStream.subscribe(self, classOf[Lobby.Event])
  }

  // user can observe only one lobby at once
  private val lobbyIdByObserver = mutable.Map.empty[ActorRef, String]
  private def observersByLobbyId() = lobbyIdByObserver.groupMap(_._2)(_._1)

  private def observe(lobbyId: String, user: ActorRef): Unit =
    lobbyIdByObserver(user) = lobbyId

  private def stopObserving(user: ActorRef): Unit =
    lobbyIdByObserver.remove(user)

  def getObservers(lobbyId: String): Set[ActorRef] =
    observersByLobbyId().getOrElse(lobbyId, Set.empty).toSet

  def receive: Receive = {
    case Observe(lobbyId, user) =>
      observe(lobbyId, user)
      context.watch(sender())
    case Terminated(user) =>
      stopObserving(user)
    case e: Lobby.Event =>
      val lobbyId = e.id
      val lobbyState = Await.result(lobbyService.getLobby(lobbyId), atMost)
      val response = WebsocketLobbyResponse(LobbyResponseType.Lobby, StatusCodes.OK.intValue, lobbyState.toJson.toString)
      getObservers(lobbyId).foreach(_ ! OutgoingMessage(response.toJson.toString))
  }
}
