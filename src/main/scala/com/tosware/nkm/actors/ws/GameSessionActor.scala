package com.tosware.nkm.actors.ws

import akka.actor.Props
import akka.http.scaladsl.model.StatusCodes
import com.tosware.nkm.actors._
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
    context.system.eventStream.subscribe(self, classOf[Game.Event])
  }

  override def receive: Receive = super.receive.orElse[Any, Unit]{
    case e: Game.Event =>
      val lobbyId = e.id

      // TODO: use async proprely
      getObservers(lobbyId).foreach { ob =>
        val authStatus = getAuthStatus(ob)
//        val lastGameLogIndex = getLastGameLogIndex(ob)
        val gameStateView = aw(gameService.getGameStateView(lobbyId, authStatus))
//        val gameLog = gameStateView.
        val response = WebsocketGameResponse(GameResponseType.State, StatusCodes.OK.intValue, gameStateView.toJson.toString)
//        val response = WebsocketGameResponse(GameResponseType.RecentGameEvents, StatusCodes.OK.intValue, gameState.toJson.toString)

        ob ! WebsocketUser.OutgoingMessage(response.toJson.toString)
      }
  }
}
