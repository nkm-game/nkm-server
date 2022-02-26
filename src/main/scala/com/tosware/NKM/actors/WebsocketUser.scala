package com.tosware.NKM.actors

import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill, Props}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import com.tosware.NKM.NKMTimeouts
import com.tosware.NKM.models.lobby._
import com.tosware.NKM.serializers.NKMJsonProtocol
import com.tosware.NKM.services.LobbyService
import com.tosware.NKM.services.http.directives.{JwtHelper, JwtSecretKey}
import spray.json._

import scala.concurrent.Await

object WebsocketUser {
  case object GetAuthStatus
  case class AuthStatus(username: Option[String])
  case class Connected(outgoing: ActorRef)
  case class IncomingMessage(text: String)
  case class OutgoingMessage(text: String)
  case class Authenticate(username: String)
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

    {
      case GetAuthStatus =>
        log.info("get auth status")
        sender() ! AuthStatus(username)
      case Authenticate(u) =>
        username = Some(u)
      case IncomingMessage(text) =>
        try {
          val request = text.parseJson.convertTo[WebsocketLobbyRequest]
          log.info(s"Request: $request")
          val response = parseWebsocketLobbyRequest(request, outgoing, self, AuthStatus(username))
          log.info(s"Response: $response")
          outgoing ! OutgoingMessage(response.toJson.toString)
        }
        catch {
          case e: Exception =>
            log.error(e.toString)
            val response = WebsocketLobbyResponse(LobbyResponseType.Error, StatusCodes.InternalServerError.intValue, "Error with request parsing.")
            outgoing ! OutgoingMessage(response.toJson.toString)
        }
      case PoisonPill =>
        log.info(s"Disconnected")
    }
  }

  def parseWebsocketLobbyRequest(request: WebsocketLobbyRequest, outgoing: ActorRef, userActor: ActorRef, authStatus: AuthStatus): WebsocketLobbyResponse = {
    request.requestPath match {
      case LobbyRoute.Auth =>
        val token = request.requestJson.parseJson.convertTo[AuthRequest].token
        authenticateToken(token) match {
          case Some(username) =>
            userActor ! WebsocketUser.Authenticate(username)
            WebsocketLobbyResponse(LobbyResponseType.Auth, StatusCodes.OK.intValue, username)
          case None =>
            WebsocketLobbyResponse(LobbyResponseType.Auth, StatusCodes.Unauthorized.intValue, "Invalid token.")
        }
      case LobbyRoute.Observe =>
        val lobbyId = request.requestJson.parseJson.convertTo[ObserveRequest].lobbyId
        lobbySession ! LobbySessionActor.Observe(lobbyId, outgoing)
        WebsocketLobbyResponse(LobbyResponseType.Observe, StatusCodes.OK.intValue)
      case LobbyRoute.Lobbies =>
        val lobbies = Await.result(lobbyService.getAllLobbies(), atMost)
        WebsocketLobbyResponse(LobbyResponseType.Lobbies, StatusCodes.OK.intValue, lobbies.toJson.toString)
      case LobbyRoute.Lobby =>
        val lobbyId = request.requestJson.parseJson.convertTo[GetLobbyRequest].lobbyId
        val lobby = Await.result(lobbyService.getLobby(lobbyId), atMost)
        WebsocketLobbyResponse(LobbyResponseType.Lobby, StatusCodes.OK.intValue, lobby.toJson.toString)
      case LobbyRoute.CreateLobby =>
        val lobbyName = request.requestJson.parseJson.convertTo[LobbyCreationRequest].name
        authStatus match {
          case AuthStatus(Some(username)) =>
            lobbyService.createLobby(lobbyName, username) match {
              case LobbyService.LobbyCreated(lobbyId) => WebsocketLobbyResponse(LobbyResponseType.CreateLobby, StatusCodes.Created.intValue, lobbyId)
              case LobbyService.LobbyCreationFailure => WebsocketLobbyResponse(LobbyResponseType.CreateLobby, StatusCodes.InternalServerError.intValue)
              case _ => WebsocketLobbyResponse(LobbyResponseType.CreateLobby, StatusCodes.InternalServerError.intValue)
            }
          case _ => WebsocketLobbyResponse(LobbyResponseType.CreateLobby, StatusCodes.Unauthorized.intValue)
        }
      case LobbyRoute.JoinLobby =>
        val entity = request.requestJson.parseJson.convertTo[LobbyJoinRequest]
        authStatus match {
          case AuthStatus(Some(username)) =>
            lobbyService.joinLobby(username, entity) match {
              case LobbyService.Success => WebsocketLobbyResponse(LobbyResponseType.JoinLobby, StatusCodes.OK.intValue)
              case LobbyService.Failure => WebsocketLobbyResponse(LobbyResponseType.JoinLobby, StatusCodes.InternalServerError.intValue)
              case _ => WebsocketLobbyResponse(LobbyResponseType.JoinLobby, StatusCodes.InternalServerError.intValue)
            }
          case _ => WebsocketLobbyResponse(LobbyResponseType.JoinLobby, StatusCodes.Unauthorized.intValue)
        }
      case LobbyRoute.LeaveLobby =>
        val entity = request.requestJson.parseJson.convertTo[LobbyLeaveRequest]
        authStatus match {
          case AuthStatus(Some(username)) =>
            lobbyService.leaveLobby(username, entity) match {
              case LobbyService.Success => WebsocketLobbyResponse(LobbyResponseType.LeaveLobby, StatusCodes.OK.intValue)
              case LobbyService.Failure => WebsocketLobbyResponse(LobbyResponseType.LeaveLobby, StatusCodes.InternalServerError.intValue)
              case _ => WebsocketLobbyResponse(LobbyResponseType.LeaveLobby, StatusCodes.InternalServerError.intValue)
            }
          case _ => WebsocketLobbyResponse(LobbyResponseType.LeaveLobby, StatusCodes.Unauthorized.intValue)
        }
      case LobbyRoute.SetHexMap =>
        val entity = request.requestJson.parseJson.convertTo[SetHexMapNameRequest]
        authStatus match {
          case AuthStatus(Some(username)) =>
            lobbyService.setHexmapName(username, entity) match {
              case LobbyService.Success => WebsocketLobbyResponse(LobbyResponseType.SetHexMap, StatusCodes.OK.intValue)
              case LobbyService.Failure => WebsocketLobbyResponse(LobbyResponseType.SetHexMap, StatusCodes.InternalServerError.intValue)
              case _ => WebsocketLobbyResponse(LobbyResponseType.SetHexMap, StatusCodes.InternalServerError.intValue)
            }
          case _ => WebsocketLobbyResponse(LobbyResponseType.SetHexMap, StatusCodes.Unauthorized.intValue)
        }
      case LobbyRoute.SetPickType =>
        val entity = request.requestJson.parseJson.convertTo[SetPickTypeRequest]
        authStatus match {
          case AuthStatus(Some(username)) =>
            lobbyService.setPickType(username, entity) match {
              case LobbyService.Success => WebsocketLobbyResponse(LobbyResponseType.SetPickType, StatusCodes.OK.intValue)
              case LobbyService.Failure => WebsocketLobbyResponse(LobbyResponseType.SetPickType, StatusCodes.InternalServerError.intValue)
              case _ => WebsocketLobbyResponse(LobbyResponseType.SetPickType, StatusCodes.InternalServerError.intValue)
            }
          case _ => WebsocketLobbyResponse(LobbyResponseType.SetPickType, StatusCodes.Unauthorized.intValue)
        }
      case LobbyRoute.SetNumberOfBans =>
        val entity = request.requestJson.parseJson.convertTo[SetNumberOfBansRequest]
        authStatus match {
          case AuthStatus(Some(username)) =>
            lobbyService.setNumberOfBans(username, entity) match {
              case LobbyService.Success => WebsocketLobbyResponse(LobbyResponseType.SetNumberOfBans, StatusCodes.OK.intValue)
              case LobbyService.Failure => WebsocketLobbyResponse(LobbyResponseType.SetNumberOfBans, StatusCodes.InternalServerError.intValue)
              case _ => WebsocketLobbyResponse(LobbyResponseType.SetNumberOfBans, StatusCodes.InternalServerError.intValue)
            }
          case _ => WebsocketLobbyResponse(LobbyResponseType.SetNumberOfBans, StatusCodes.Unauthorized.intValue)
        }
      case LobbyRoute.SetNumberOfCharacters =>
        val entity = request.requestJson.parseJson.convertTo[SetNumberOfCharactersPerPlayerRequest]
        authStatus match {
          case AuthStatus(Some(username)) =>
            lobbyService.setNumberOfCharactersPerPlayer(username, entity) match {
              case LobbyService.Success => WebsocketLobbyResponse(LobbyResponseType.SetNumberOfCharacters, StatusCodes.OK.intValue)
              case LobbyService.Failure => WebsocketLobbyResponse(LobbyResponseType.SetNumberOfCharacters, StatusCodes.InternalServerError.intValue)
              case _ => WebsocketLobbyResponse(LobbyResponseType.SetNumberOfCharacters, StatusCodes.InternalServerError.intValue)
            }
          case _ => WebsocketLobbyResponse(LobbyResponseType.SetNumberOfCharacters, StatusCodes.Unauthorized.intValue)
        }
      case LobbyRoute.SetLobbyName =>
        val entity = request.requestJson.parseJson.convertTo[SetLobbyNameRequest]
        authStatus match {
          case AuthStatus(Some(username)) =>
            lobbyService.setLobbyName(username, entity) match {
              case LobbyService.Success => WebsocketLobbyResponse(LobbyResponseType.SetLobbyName, StatusCodes.OK.intValue)
              case LobbyService.Failure => WebsocketLobbyResponse(LobbyResponseType.SetLobbyName, StatusCodes.InternalServerError.intValue)
              case _ => WebsocketLobbyResponse(LobbyResponseType.SetLobbyName, StatusCodes.InternalServerError.intValue)
            }
          case _ => WebsocketLobbyResponse(LobbyResponseType.SetNumberOfCharacters, StatusCodes.Unauthorized.intValue)
        }
      case LobbyRoute.StartGame =>
        val entity = request.requestJson.parseJson.convertTo[StartGameRequest]
        authStatus match {
          case AuthStatus(Some(username)) =>
            lobbyService.startGame(username, entity) match {
              case LobbyService.Success => WebsocketLobbyResponse(LobbyResponseType.StartGame, StatusCodes.OK.intValue)
              case LobbyService.Failure => WebsocketLobbyResponse(LobbyResponseType.StartGame, StatusCodes.InternalServerError.intValue)
              case _ => WebsocketLobbyResponse(LobbyResponseType.StartGame, StatusCodes.InternalServerError.intValue)
            }
          case _ => WebsocketLobbyResponse(LobbyResponseType.StartGame, StatusCodes.Unauthorized.intValue)
        }
    }
  }

}
