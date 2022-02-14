package com.tosware.NKM.services.http.routes

import akka.NotUsed
import akka.actor.{Actor, ActorRef, ActorSystem, PoisonPill, Props, Terminated}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.http.scaladsl.server.Directives._
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.tosware.NKM.NKMTimeouts
import com.tosware.NKM.actors.{LobbySessionActor, WebsocketUser}
import com.tosware.NKM.models.lobby.{AuthRequest, GetLobbyRequest, LobbyCreationRequest, LobbyRequest}
import com.tosware.NKM.serializers.NKMJsonProtocol
import com.tosware.NKM.services.LobbyService
import com.tosware.NKM.services.http.directives.JwtDirective
import spray.json._

import scala.concurrent.Await

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


case class WebsocketLobbyRequest(requestPath: LobbyRoute, requestJson: String)
case class WebsocketLobbyResponse(statusCode: Int, body: String = "")

trait WebsocketRoutes extends JwtDirective
  with SprayJsonSupport
  with NKMJsonProtocol
  with NKMTimeouts
{
  implicit val system: ActorSystem
  implicit val lobbyService: LobbyService

  lazy val lobbySession = system.actorOf(LobbySessionActor.props(), "lobby")

  def newUser() = {
    // new connection - new user actor
    val userActor = system.actorOf(WebsocketUser.props(lobbySession))

    val incomingMessages =
      Flow[Message].map {
        case TextMessage.Strict(text) => WebsocketUser.IncomingMessage(text)
      }.to(Sink.actorRef[WebsocketUser.IncomingMessage](userActor, PoisonPill))

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

  def parseWebsocketLobbyRequest(request: WebsocketLobbyRequest): WebsocketLobbyResponse = {
//      val request = text.parseJson.convertTo[WebsocketLobbyRequest]
      request.requestPath match {
        case LobbyRoute.Auth =>
          val token = request.requestJson.parseJson.convertTo[AuthRequest].token
          WebsocketLobbyResponse(200)
        case LobbyRoute.Lobbies =>
          val lobbies = Await.result(lobbyService.getAllLobbies(), atMost)
          WebsocketLobbyResponse(200, lobbies.toJson.toString)
        case LobbyRoute.Lobby =>
          val lobbyId = request.requestJson.parseJson.convertTo[GetLobbyRequest].lobbyId
          val lobby = Await.result(lobbyService.getLobby(lobbyId), atMost)
          WebsocketLobbyResponse(200, lobby.toJson.toString)
        case LobbyRoute.CreateLobby => ???
        case LobbyRoute.JoinLobby => ???
        case LobbyRoute.LeaveLobby => ???
        case LobbyRoute.SetHexMap => ???
        case LobbyRoute.SetPickType => ???
        case LobbyRoute.SetNumberOfBans => ???
        case LobbyRoute.SetNumberOfCharacters => ???
        case LobbyRoute.StartGame => ???
      }
  }

  val websocketRoutes = concat (
    path("lobby") {
      handleWebSocketMessages(newUser())
    },
  )
}
