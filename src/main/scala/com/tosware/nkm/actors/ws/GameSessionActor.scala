package com.tosware.nkm.actors.ws

import akka.actor.{ActorRef, Props}
import akka.http.scaladsl.model.StatusCodes
import com.tosware.nkm.models.GameEventMapped
import com.tosware.nkm.models.game.GameEvent.GameEvent
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

  private def sendGameEvent(observer: ActorRef, e: GameEvent): Unit = {
    val response = WebsocketGameResponse(GameResponseType.Event, StatusCodes.OK.intValue, e.toJson.toString)
    observer ! WebsocketUser.OutgoingMessage(response.toJson.toString)
  }

  override def receive: Receive = super.receive.orElse[Any, Unit]{
    case e: GameEventMapped =>
      // TODO: use async proprely
      getObservers(e.gameId).foreach { ob =>
        val authStatus = getAuthStatus(ob)
        val sendEvent = e.hideData.isEmpty || authStatus.nonEmpty && e.hideData.get.showOnlyFor.contains(authStatus.get)
        if(sendEvent) {
          sendGameEvent(ob, e.event)
        }
      }
  }
}
