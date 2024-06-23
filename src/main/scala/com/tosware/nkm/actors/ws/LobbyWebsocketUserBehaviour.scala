package com.tosware.nkm.actors.ws

import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCodes
import com.tosware.nkm.Logging
import com.tosware.nkm.models.CommandResponse
import com.tosware.nkm.models.CommandResponse.CommandResponse
import com.tosware.nkm.models.lobby.ws.*
import com.tosware.nkm.services.LobbyService
import com.tosware.nkm.services.http.directives.JwtSecretKey
import spray.json.*

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Try

trait LobbyWebsocketUserBehaviour extends WebsocketUserBehaviour {
  val session: ActorRef
  implicit val lobbyService: LobbyService
  implicit val jwtSecretKey: JwtSecretKey

  import WebsocketUser.*

  override def parseIncomingMessage(outgoing: ActorRef, username: Option[String], text: String): Unit =
    Logging.withLobbyContext("Websocket") {
      try {
        val request = text.parseJson.convertTo[WebsocketLobbyRequest]
        if (request.requestPath != LobbyRoute.Ping) {
          log.info(s"[${username.getOrElse("")}] ${request.requestPath}")
        }
        log.debug(s"Request: $request")
        val response = parseWebsocketLobbyRequest(request, outgoing, self, AuthStatus(username))
        if (response.lobbyResponseType != LobbyResponse.Ping) {
          log.info(s"[${username.getOrElse("")}] ${response.lobbyResponseType}(${response.statusCode})")
        }
        log.debug(s"Response: $response")
        outgoing ! OutgoingMessage(response.toJson.toString)
      } catch {
        case e: Exception =>
          log.error(e.toString)
          val response = WebsocketLobbyResponse(
            LobbyResponse.Error,
            StatusCodes.InternalServerError.intValue,
            "Error with request parsing.",
          )
          outgoing ! OutgoingMessage(response.toJson.toString)
      }
    }

  def ok(msg: String = "")(implicit responseType: LobbyResponse): WebsocketLobbyResponse =
    WebsocketLobbyResponse(responseType, StatusCodes.OK.intValue, msg)

  def nok(msg: String = "")(implicit responseType: LobbyResponse): WebsocketLobbyResponse =
    WebsocketLobbyResponse(responseType, StatusCodes.InternalServerError.intValue, msg)

  def unauthorized(msg: String = "")(implicit responseType: LobbyResponse): WebsocketLobbyResponse =
    WebsocketLobbyResponse(responseType, StatusCodes.Unauthorized.intValue, msg)

  def notFound(msg: String = "")(implicit responseType: LobbyResponse): WebsocketLobbyResponse =
    WebsocketLobbyResponse(responseType, StatusCodes.NotFound.intValue, msg)

  def resolveResponse(commandResponse: CommandResponse)(
      implicit responseType: LobbyResponse
  ): WebsocketLobbyResponse =
    commandResponse match {
      case CommandResponse.Success(msg) => ok(msg)
      case CommandResponse.Failure(msg) => nok(msg)
    }

  def parseWebsocketLobbyRequest(
      request: WebsocketLobbyRequest,
      outgoing: ActorRef,
      userActor: ActorRef,
      authStatus: AuthStatus,
  ): WebsocketLobbyResponse = {
    import LobbyRequest.*

    def parseJson[T: JsonFormat](f: T => WebsocketLobbyResponse)(
        implicit responseType: LobbyResponse
    ): WebsocketLobbyResponse =
      Try(request.requestJson.parseJson.convertTo[T]) match {
        case util.Success(parsed) => f(parsed)
        case util.Failure(_)      => nok("Invalid request.")
      }

    def handleGetLobby(getLobby: GetLobby) = {
      implicit val responseType: LobbyResponse = LobbyResponse.GetLobby
      lobbyService.getLobbyStateOpt(getLobby.lobbyId) match {
        case Some(lobbyFuture) => ok(aw(lobbyFuture).toJson.toString)
        case None              => notFound()
      }
    }

    def handleAuthLobbyOperation[T <: LobbyRequest: JsonFormat](operation: (String, T) => CommandResponse)(
        entity: T
    )(implicit responseType: LobbyResponse): WebsocketLobbyResponse =
      if (authStatus.userIdOpt.isDefined) {
        val username = authStatus.userIdOpt.get
        resolveResponse(operation(username, entity))
      } else unauthorized()

    request.requestPath match {
      case LobbyRoute.Ping =>
        implicit val responseType: LobbyResponse = LobbyResponse.Ping
        ok("pong")

      case LobbyRoute.Auth =>
        implicit val responseType: LobbyResponse = LobbyResponse.Auth
        parseJson[Auth] { auth =>
          authenticateToken(auth.token) match {
            case Some(userStateView) =>
              userActor ! WebsocketUser.Authenticate(userStateView.email)
              ok(userStateView.email)
            case None => unauthorized("Invalid token.")
          }
        }

      case LobbyRoute.Observe =>
        implicit val responseType: LobbyResponse = LobbyResponse.Observe
        parseJson[Observe] { observe =>
          session ! SessionActor.Observe(observe.lobbyId, outgoing)
          ok()
        }

      case LobbyRoute.GetLobbies =>
        implicit val responseType: LobbyResponse = LobbyResponse.GetLobbies
        aw(lobbyService.getAllLobbies().map(lobbies => ok(lobbies.toJson.toString)))

      case LobbyRoute.GetLobby =>
        implicit val responseType: LobbyResponse = LobbyResponse.GetLobby
        parseJson[GetLobby](handleGetLobby)

      case LobbyRoute.CreateLobby =>
        implicit val responseType: LobbyResponse = LobbyResponse.CreateLobby
        parseJson[CreateLobby](entity => handleAuthLobbyOperation(lobbyService.createLobby)(entity))

      case LobbyRoute.JoinLobby =>
        implicit val responseType: LobbyResponse = LobbyResponse.JoinLobby
        parseJson[JoinLobby](entity => handleAuthLobbyOperation(lobbyService.joinLobby)(entity))

      case LobbyRoute.LeaveLobby =>
        implicit val responseType: LobbyResponse = LobbyResponse.LeaveLobby
        parseJson[LeaveLobby](entity => handleAuthLobbyOperation(lobbyService.leaveLobby)(entity))

      case LobbyRoute.SetHexMap =>
        implicit val responseType: LobbyResponse = LobbyResponse.SetHexMap
        parseJson[SetHexMapName](entity => handleAuthLobbyOperation(lobbyService.setHexmapName)(entity))

      case LobbyRoute.SetGameMode =>
        implicit val responseType: LobbyResponse = LobbyResponse.SetGameMode
        parseJson[SetGameMode](entity => handleAuthLobbyOperation(lobbyService.setGameMode)(entity))

      case LobbyRoute.SetPickType =>
        implicit val responseType: LobbyResponse = LobbyResponse.SetPickType
        parseJson[SetPickType](entity => handleAuthLobbyOperation(lobbyService.setPickType)(entity))

      case LobbyRoute.SetNumberOfBans =>
        implicit val responseType: LobbyResponse = LobbyResponse.SetNumberOfBans
        parseJson[SetNumberOfBans](entity => handleAuthLobbyOperation(lobbyService.setNumberOfBans)(entity))

      case LobbyRoute.SetNumberOfCharactersPerPlayer =>
        implicit val responseType: LobbyResponse = LobbyResponse.SetNumberOfCharactersPerPlayer
        parseJson[SetNumberOfCharactersPerPlayer](entity =>
          handleAuthLobbyOperation(lobbyService.setNumberOfCharactersPerPlayer)(entity)
        )

      case LobbyRoute.SetLobbyName =>
        implicit val responseType: LobbyResponse = LobbyResponse.SetLobbyName
        parseJson[SetLobbyName](entity => handleAuthLobbyOperation(lobbyService.setLobbyName)(entity))

      case LobbyRoute.SetClockConfig =>
        implicit val responseType: LobbyResponse = LobbyResponse.SetClockConfig
        parseJson[SetClockConfig](entity => handleAuthLobbyOperation(lobbyService.setClockConfig)(entity))

      case LobbyRoute.SetColor =>
        implicit val responseType: LobbyResponse = LobbyResponse.SetColor
        parseJson[SetColor](entity => handleAuthLobbyOperation(lobbyService.setColor)(entity))

      case LobbyRoute.StartGame =>
        implicit val responseType: LobbyResponse = LobbyResponse.StartGame
        parseJson[StartGame](entity => handleAuthLobbyOperation(lobbyService.startGame)(entity))
    }
  }
}
