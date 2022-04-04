package com.tosware.NKM.services.http.routes

import akka.NotUsed
import akka.actor.{ActorRef, ActorSystem, PoisonPill}
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Directives._
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.tosware.NKM.actors.ws._
import com.tosware.NKM.services.http.directives.JwtDirective
import com.tosware.NKM.services.{GameService, LobbyService}

trait WebsocketRoutes extends JwtDirective
{
  implicit val system: ActorSystem
  implicit val lobbyService: LobbyService
  implicit val gameService: GameService

  lazy val lobbySessionActor = system.actorOf(LobbySessionActor.props(), "lobby_session")
  lazy val gameSessionActor = system.actorOf(GameSessionActor.props(), "game_session")

  // new connection - new user actor
  def newUser(userActor: ActorRef) = {
    val onFailureMessage = (onFailureMessage: Throwable) => println(onFailureMessage.getMessage)

    val incomingMessages =
      Flow[Message].map {
        case TextMessage.Strict(text) => WebsocketUser.IncomingMessage(text)
      }.to(Sink.actorRef[WebsocketUser.IncomingMessage](userActor, PoisonPill, onFailureMessage))

    val outgoingMessages =
      Source.actorRef[WebsocketUser.OutgoingMessage](10, OverflowStrategy.fail)
        .mapMaterializedValue { outActor =>
          userActor ! WebsocketUser.Connected(outActor)
          NotUsed
        }.map(
        (outMsg: WebsocketUser.OutgoingMessage) => TextMessage(outMsg.text))

    // then combine both to a flow
    Flow.fromSinkAndSource(incomingMessages, outgoingMessages)
  }


  val websocketRoutes = concat (
    path("lobby") {
      // new connection - new user actor
      handleWebSocketMessages(newUser(system.actorOf(WebsocketUser.lobbyProps(lobbySessionActor))))
    },
    path("game") {
      // new connection - new user actor
      handleWebSocketMessages(newUser(system.actorOf(WebsocketUser.gameProps(gameSessionActor))))
    },
  )
}
