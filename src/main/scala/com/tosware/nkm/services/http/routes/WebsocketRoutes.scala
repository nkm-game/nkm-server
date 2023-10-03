package com.tosware.nkm.services.http.routes

import akka.actor.{ActorRef, ActorSystem, PoisonPill}
import akka.http.scaladsl.model.ws.*
import akka.http.scaladsl.server.Directives.*
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.stream.{CompletionStrategy, OverflowStrategy}
import akka.{Done, NotUsed}
import com.tosware.nkm.actors.ws.*
import com.tosware.nkm.services.http.directives.{JwtDirective, JwtSecretKey}
import com.tosware.nkm.services.{GameService, LobbyService}
import com.tosware.nkm.{Logging, NkmDependencies}

class WebsocketRoutes(deps: NkmDependencies) extends JwtDirective with Logging {
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
        case message: TextMessage =>
          logger.error("user sent non strict text message: " + message)
          ???
        case message: BinaryMessage =>
          logger.error("user sent binary message: " + message)
          ???
      }.to(Sink.actorRef[WebsocketUser.IncomingMessage](userActor, PoisonPill, onFailureMessage))

    val outgoingMessages = Source.actorRef(
      completionMatcher = {
        case Done =>
          // complete stream immediately if we send it Done
          CompletionStrategy.immediately
      },
      // never fail the stream because of a message
      failureMatcher = PartialFunction.empty,
      bufferSize = 100,
      overflowStrategy = OverflowStrategy.dropHead,
    ).mapMaterializedValue { outActor =>
      userActor ! WebsocketUser.Connected(outActor)
      NotUsed
    }.map((outMsg: WebsocketUser.OutgoingMessage) => TextMessage(outMsg.text))

    // then combine both to a flow
    Flow.fromSinkAndSource(incomingMessages, outgoingMessages)
  }

  val websocketRoutes = concat(
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
