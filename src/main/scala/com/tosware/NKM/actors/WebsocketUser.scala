package com.tosware.NKM.actors

import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill, Props}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{StatusCode, StatusCodes}
import akka.pattern.ask
import com.tosware.NKM.NKMTimeouts
import com.tosware.NKM.models.lobby.{AuthRequest, GetLobbyRequest, LobbyCreationRequest}
import com.tosware.NKM.serializers.NKMJsonProtocol
import com.tosware.NKM.services.LobbyService
import com.tosware.NKM.services.http.directives.{JwtHelper, JwtSecretKey}
import com.tosware.NKM.services.http.routes.LobbyRoute

import scala.concurrent.Await
import spray.json._

object WebsocketUser {
  case object GetAuthStatus
  case class AuthStatus(username: Option[String])
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
      case GetAuthStatus =>
        log.info("get auth status")
        sender() ! AuthStatus(username)
      case Authenticate(u) =>
        username = Some(u)
      case IncomingMessage(text) =>
        val request = text.parseJson.convertTo[WebsocketLobbyRequest]
        log.info(s"Request: $request")
        val response = parseWebsocketLobbyRequest(request, self, AuthStatus(username))
        log.info(s"Response: $response")
        outgoing ! OutgoingMessage(response.toJson.toString)
      case PoisonPill =>
        log.info(s"Disconnected")
    }
  }

  def parseWebsocketLobbyRequest(request: WebsocketLobbyRequest, userActor: ActorRef, authStatus: AuthStatus): WebsocketLobbyResponse = {
    request.requestPath match {
      case LobbyRoute.Auth =>
        val token = request.requestJson.parseJson.convertTo[AuthRequest].token
        authenticateToken(token) match {
          case Some(username) =>
            userActor ! WebsocketUser.Authenticate(username)
            WebsocketLobbyResponse(StatusCodes.OK.intValue, username)
          case None =>
            WebsocketLobbyResponse(StatusCodes.Unauthorized.intValue, "Invalid token.")
        }

      case LobbyRoute.Lobbies =>
        val lobbies = Await.result(lobbyService.getAllLobbies(), atMost)
        WebsocketLobbyResponse(StatusCodes.OK.intValue, lobbies.toJson.toString)
      case LobbyRoute.Lobby =>
        val lobbyId = request.requestJson.parseJson.convertTo[GetLobbyRequest].lobbyId
        val lobby = Await.result(lobbyService.getLobby(lobbyId), atMost)
        WebsocketLobbyResponse(StatusCodes.OK.intValue, lobby.toJson.toString)
      case LobbyRoute.CreateLobby =>
        val lobbyName = request.requestJson.parseJson.convertTo[LobbyCreationRequest].name
        authStatus match {
          case AuthStatus(Some(username)) =>
            lobbyService.createLobby(lobbyName, username) match {
              case LobbyService.LobbyCreated(lobbyId) => WebsocketLobbyResponse(StatusCodes.Created.intValue, lobbyId)
              case LobbyService.LobbyCreationFailure => WebsocketLobbyResponse(StatusCodes.InternalServerError.intValue)
              case _ => WebsocketLobbyResponse(StatusCodes.InternalServerError.intValue)
            }
          case _ => WebsocketLobbyResponse(StatusCodes.Unauthorized.intValue)
        }
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
