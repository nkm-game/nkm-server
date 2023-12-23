package com.tosware.nkm.actors.ws

import akka.actor.Props
import akka.http.scaladsl.model.StatusCodes
import com.tosware.nkm.actors.Lobby
import com.tosware.nkm.models.lobby.ws.*
import com.tosware.nkm.services.LobbyService
import spray.json.*
import cats.implicits.*

object LobbySessionActor {
  def props()(implicit lobbyService: LobbyService): Props = Props(new LobbySessionActor)
}

class LobbySessionActor(implicit val lobbyService: LobbyService)
    extends SessionActor {
  override def preStart(): Unit = {
    log.info("LobbySessionActor started")
    context.system.eventStream.subscribe(self, classOf[Lobby.Event])
  }

  override def receive: Receive = super.receive.orElse[Any, Unit] {
    case e: Lobby.Event =>
      import scala.concurrent.ExecutionContext.Implicits.global

      val lobbyId = e.id
      val lobbyStateOpt = aw(lobbyService.getLobbyStateOpt(lobbyId).traverse(identity))
      lobbyStateOpt foreach { lobbyState =>
        val response =
          WebsocketLobbyResponse(LobbyResponse.GetLobby, StatusCodes.OK.intValue, lobbyState.toJson.toString)
        getObservers(lobbyId).foreach(_ ! WebsocketUser.OutgoingMessage(response.toJson.toString))
      }
  }
}
