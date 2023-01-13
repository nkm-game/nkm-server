package com.tosware.nkm.actors.ws

import akka.actor.Props
import akka.http.scaladsl.model.StatusCodes
import com.tosware.nkm.models.GameEventMapped
import com.tosware.nkm.models.game.ws._
import com.tosware.nkm.services._
import spray.json._


object GameSessionActor {
  def props()(implicit gameService: GameService): Props = Props(new GameSessionActor())
}

class GameSessionActor(implicit gameService: GameService)
  extends SessionActor
{
  override def preStart(): Unit = {
    log.info("GameSessionActor started")
    context.system.eventStream.subscribe(self, classOf[GameEventMapped])
  }

  override def receive: Receive = super.receive.orElse[Any, Unit]{
    case e: GameEventMapped =>
      // TODO: use async proprely
      getObservers(e.gameId).foreach { ob =>
        val authStatus = getAuthStatus(ob)
        if(!e.event.hiddenFor(authStatus)) {
          val response = WebsocketGameResponse(GameResponseType.Event, StatusCodes.OK.intValue, e.toJson.toString)
          ob ! WebsocketUser.OutgoingMessage(response.toJson.toString)
        }
      }
  }
}
