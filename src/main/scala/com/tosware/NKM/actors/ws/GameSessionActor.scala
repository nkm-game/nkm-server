package com.tosware.NKM.actors.ws

import akka.actor.Props
import akka.http.scaladsl.model.StatusCodes
import com.tosware.NKM.actors._
import com.tosware.NKM.models.game.ws._
import com.tosware.NKM.services._
import spray.json._

import scala.concurrent.Await


object GameSessionActor {
  def props()(implicit gameService: GameService): Props = Props(new GameSessionActor())
}

class GameSessionActor(implicit gameService: GameService)
  extends SessionActor
{
  override def preStart(): Unit = {
    log.info("GameSessionActor started")
    context.system.eventStream.subscribe(self, classOf[Game.Event])
  }

  override def receive: Receive = super.receive.orElse[Any, Unit]{
    case e: Game.Event =>
      val gameId = e.id
      val gameState = Await.result(gameService.getGameState(gameId), atMost)
      val response = WebsocketGameResponse(GameResponseType.State, StatusCodes.OK.intValue, gameState.toJson.toString)
      getObservers(gameId).foreach(_ ! WebsocketUser.OutgoingMessage(response.toJson.toString))
  }
}
