package com.tosware.nkm.services.http.routes

import akka.NotUsed
import akka.actor.{ActorRef, ActorSystem, PoisonPill}
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.http.scaladsl.server.Directives._
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.tosware.nkm.actors.ws._
import com.tosware.nkm.services.http.directives.{JwtDirective, JwtSecretKey}
import com.tosware.nkm.services.{GameService, LobbyService}
import com.tosware.nkm.{Logging, NkmDependencies}

import scala.annotation.nowarn

class WebsocketRoutes(deps: NkmDependencies) extends JwtDirective with Logging
{
  implicit val jwtSecretKey: JwtSecretKey = deps.jwtSecretKey
  implicit val system: ActorSystem = deps.system
  implicit val lobbyService: LobbyService = deps.lobbyService
  implicit val gameService: GameService = deps.gameService

  // new connection - new user actor
  def newUser(userActor: ActorRef) = {
    val onFailureMessage = (onFailureMessage: Throwable) => logger.error(onFailureMessage.getMessage)

    val incomingMessages =
      Flow[Message].map {
        case TextMessage.Strict(text) => WebsocketUser.IncomingMessage(text)
        case message: TextMessage => ???
        case message: BinaryMessage => ???
      }.to(Sink.actorRef[WebsocketUser.IncomingMessage](userActor, PoisonPill, onFailureMessage))

    @nowarn("cat=deprecation")
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
      handleWebSocketMessages(newUser(system.actorOf(WebsocketUser.lobbyProps(deps.lobbySessionActor))))
    },
    path("game") {
      // new connection - new user actor
      handleWebSocketMessages(newUser(system.actorOf(WebsocketUser.gameProps(deps.gameSessionActor))))
    },
  )
}
