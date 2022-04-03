package com.tosware.NKM.actors

import akka.actor.{ActorRef, Props}
import akka.http.scaladsl.model.StatusCodes
import com.tosware.NKM.actors.WebsocketUser.OutgoingMessage
import com.tosware.NKM.models.lobby.{LobbyResponseType, WebsocketLobbyResponse}
import com.tosware.NKM.services.LobbyService
import spray.json._

import scala.concurrent.Await

object LobbySessionActor {
  def props()(implicit lobbyService: LobbyService): Props = Props(new LobbySessionActor)
}

class LobbySessionActor(implicit val lobbyService: LobbyService)
  extends SessionActor[Lobby.Event]
{
  override def preStart(): Unit = {
    log.info("LobbySessionActor started")
    context.system.eventStream.subscribe(self, classOf[Lobby.Event])
  }

  override def receive: Receive = super.receive.orElse[Any, Unit]{
    case e: Lobby.Event =>
      val lobbyId = e.id
      val lobbyState = Await.result(lobbyService.getLobby(lobbyId), atMost)
      val response = WebsocketLobbyResponse(LobbyResponseType.Lobby, StatusCodes.OK.intValue, lobbyState.toJson.toString)
      getObservers(lobbyId).foreach(_ ! OutgoingMessage(response.toJson.toString))
  }
}
