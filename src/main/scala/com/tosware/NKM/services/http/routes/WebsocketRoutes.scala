package com.tosware.NKM.services.http.routes

import akka.NotUsed
import akka.actor.{ActorSystem, PoisonPill}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Directives._
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.tosware.NKM.NKMTimeouts
import com.tosware.NKM.actors.{LobbySessionActor, WebsocketUser}
import com.tosware.NKM.serializers.NKMJsonProtocol
import com.tosware.NKM.services.LobbyService
import com.tosware.NKM.services.http.directives.{JwtDirective, JwtHelper}

// object to keep track of lobby sessions
//object LobbySessionMap {
//  private var sessions = Map.empty[String, LobbySession]
//
//  def findOrCreate(userId: String)(implicit system: ActorSystem) = sessions.getOrElse(userId, create(userId))
//
//  private def create(userId: String)(implicit system: ActorSystem) = {
//    val session = LobbySession(userId)
//    sessions += userId -> session
//    session
//  }
//}



trait WebsocketRoutes extends JwtDirective
{
  implicit val system: ActorSystem
  implicit val lobbyService: LobbyService

  lazy val lobbySession = system.actorOf(LobbySessionActor.props(), "lobby")

  def newUser() = {
    // new connection - new user actor
    val userActor = system.actorOf(WebsocketUser.props(lobbySession))

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
      handleWebSocketMessages(newUser())
    },
  )
}
