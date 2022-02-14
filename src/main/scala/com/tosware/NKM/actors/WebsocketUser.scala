package com.tosware.NKM.actors

import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill, Props}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.tosware.NKM.NKMTimeouts
import com.tosware.NKM.models.lobby.{AuthRequest, GetLobbyRequest}
import com.tosware.NKM.serializers.NKMJsonProtocol
import com.tosware.NKM.services.LobbyService
import com.tosware.NKM.services.http.directives.{JwtHelper, JwtSecretKey}
import com.tosware.NKM.services.http.routes.LobbyRoute

import scala.concurrent.Await
import spray.json._

object WebsocketUser {
  case class Connected(outgoing: ActorRef)
  case class IncomingMessage(text: String)
  case class OutgoingMessage(text: String)
  case class Authenticate(username: String)
  case class WebsocketLobbyRequest(requestPath: LobbyRoute, requestJson: String)
  case class WebsocketLobbyResponse(statusCode: Int, body: String = "")
  def props(lobbySession: ActorRef)(implicit lobbyService: LobbyService, jwtSecretKey: JwtSecretKey): Props = Props(new WebsocketUser(lobbySession))
}

class WebsocketUser(lobbySession: ActorRef)(implicit val lobbyService: LobbyService, implicit val jwtSecretKey: JwtSecretKey)
  extends Actor
  with ActorLogging
  with SprayJsonSupport
  with NKMJsonProtocol
  with NKMTimeouts
  with JwtHelper
{
  import WebsocketUser._

  var username: Option[String] = None

  def receive = {
    case Connected(outgoing) =>
      context.become(connected(outgoing))
  }

  def connected(outgoing: ActorRef): Receive = {
    log.info(s"Connected")
    lobbySession ! LobbySessionActor.Join

    {
      case Authenticate(u) =>
        log.info(s"Authenticated $u")
        username = Some(u)
      case IncomingMessage(text) =>
        lobbySession ! LobbySessionActor.ChatMessage(text)
        val request = text.parseJson.convertTo[WebsocketLobbyRequest]
        val response = parseWebsocketLobbyRequest(request, self)
        outgoing ! OutgoingMessage(response.toJson.toString)

      case LobbySessionActor.ChatMessage(text) =>
        outgoing ! OutgoingMessage(text)
      case PoisonPill =>
        log.info(s"Disconnected")
    }
  }

  def parseWebsocketLobbyRequest(request: WebsocketLobbyRequest, userActor: ActorRef): WebsocketLobbyResponse = {
    //      val request = text.parseJson.convertTo[WebsocketLobbyRequest]
    request.requestPath match {
      case LobbyRoute.Auth =>
        val token = request.requestJson.parseJson.convertTo[AuthRequest].token
        authenticateToken(token) match {
          case Some(username) =>
            userActor ! WebsocketUser.Authenticate(username)
            WebsocketLobbyResponse(200, username)
          case None =>
            WebsocketLobbyResponse(401, "Invalid token.")
        }

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

}
