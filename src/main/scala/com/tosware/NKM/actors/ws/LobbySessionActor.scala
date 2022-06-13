package com.tosware.NKM.actors.ws

import akka.actor.Props
import akka.http.scaladsl.model.StatusCodes
import com.tosware.NKM.actors.Lobby
import com.tosware.NKM.models.lobby.ws._
import com.tosware.NKM.services.LobbyService
import spray.json._

object LobbySessionActor {
  def props()(implicit lobbyService: LobbyService): Props = Props(new LobbySessionActor)
}

class LobbySessionActor(implicit val lobbyService: LobbyService)
  extends SessionActor
{
  override def preStart(): Unit = {
    log.info("LobbySessionActor started")
    context.system.eventStream.subscribe(self, classOf[Lobby.Event])
  }

  override def receive: Receive = super.receive.orElse[Any, Unit]{
    case e: Lobby.Event =>
      val lobbyId = e.id
      val lobbyState = aw(lobbyService.getLobbyState(lobbyId))
      val response = WebsocketLobbyResponse(LobbyResponseType.Lobby, StatusCodes.OK.intValue, lobbyState.toJson.toString)
      getObservers(lobbyId).foreach(_ ! WebsocketUser.OutgoingMessage(response.toJson.toString))
  }
}
