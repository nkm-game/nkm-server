package com.tosware.NKM.actors.ws

import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCodes
import com.tosware.NKM.models.lobby.ws._
import com.tosware.NKM.services.LobbyService
import com.tosware.NKM.services.http.directives.JwtSecretKey
import com.tosware.NKM.models.CommandResponse._
import spray.json._

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

trait LobbyWebsocketUserBehaviour extends WebsocketUserBehaviour {
  val session: ActorRef
  implicit val lobbyService: LobbyService
  implicit val jwtSecretKey: JwtSecretKey

  import WebsocketUser._

  override def parseIncomingMessage(outgoing: ActorRef, username: Option[String], text: String): Unit =
    try {
      val request = text.parseJson.convertTo[WebsocketLobbyRequest]
      log.info(s"${request.requestPath}")
      log.debug(s"Request: $request")
      val response = parseWebsocketLobbyRequest(request, outgoing, self, AuthStatus(username))
      log.info(s"${response.lobbyResponseType}(${response.statusCode})")
      log.debug(s"Response: $response")
      outgoing ! OutgoingMessage(response.toJson.toString)
    }
    catch {
      case e: Exception =>
        log.error(e.toString)
        val response = WebsocketLobbyResponse(LobbyResponseType.Error, StatusCodes.InternalServerError.intValue, "Error with request parsing.")
        outgoing ! OutgoingMessage(response.toJson.toString)
    }

  def ok(msg: String = "")(implicit responseType: LobbyResponseType): WebsocketLobbyResponse =
    WebsocketLobbyResponse(responseType, StatusCodes.OK.intValue, msg)

  def nok(msg: String = "")(implicit responseType: LobbyResponseType): WebsocketLobbyResponse =
    WebsocketLobbyResponse(responseType, StatusCodes.InternalServerError.intValue, msg)

  def unauthorized(msg: String = "")(implicit responseType: LobbyResponseType): WebsocketLobbyResponse =
    WebsocketLobbyResponse(responseType, StatusCodes.Unauthorized.intValue, msg)

  def resolveResponse(commandResponse: CommandResponse)(implicit responseType: LobbyResponseType): WebsocketLobbyResponse =
    commandResponse match {
      case Success(msg) => ok(msg)
      case Failure(msg) => nok(msg)
    }

  def parseWebsocketLobbyRequest(request: WebsocketLobbyRequest, outgoing: ActorRef, userActor: ActorRef, authStatus: AuthStatus): WebsocketLobbyResponse = {
    import LobbyRequest._

    request.requestPath match {
      case LobbyRoute.Auth =>
        implicit val responseType: LobbyResponseType = LobbyResponseType.Auth
        val token = request.requestJson.parseJson.convertTo[Auth].token
        authenticateToken(token) match {
          case Some(username) =>
            userActor ! WebsocketUser.Authenticate(username)
            ok(username)
          case None =>
            unauthorized("Invalid token.")
        }
      case LobbyRoute.Observe =>
        implicit val responseType: LobbyResponseType = LobbyResponseType.Observe
        val lobbyId = request.requestJson.parseJson.convertTo[Observe].lobbyId
        session ! SessionActor.Observe(lobbyId, outgoing)
        ok()
      case LobbyRoute.Lobbies =>
        implicit val responseType: LobbyResponseType = LobbyResponseType.Lobbies
        val lobbies = Await.result(lobbyService.getAllLobbies(), 5000.millis)
        ok(lobbies.toJson.toString)
      case LobbyRoute.Lobby =>
        implicit val responseType: LobbyResponseType = LobbyResponseType.Lobby
        val lobbyId = request.requestJson.parseJson.convertTo[GetLobby].lobbyId
        val lobby = aw(lobbyService.getLobbyState(lobbyId))
        ok(lobby.toJson.toString)
      case LobbyRoute.CreateLobby =>
        implicit val responseType: LobbyResponseType = LobbyResponseType.CreateLobby
        val lobbyName = request.requestJson.parseJson.convertTo[LobbyCreation].name
        if(authStatus.username.isEmpty) return unauthorized()
        val username = authStatus.username.get
        val response = lobbyService.createLobby(lobbyName, username)
        resolveResponse(response)
      case LobbyRoute.JoinLobby =>
        implicit val responseType: LobbyResponseType = LobbyResponseType.JoinLobby
        val entity = request.requestJson.parseJson.convertTo[LobbyJoin]
        if(authStatus.username.isEmpty) return unauthorized()
        val username = authStatus.username.get
        val response = lobbyService.joinLobby(username, entity)
        resolveResponse(response)
      case LobbyRoute.LeaveLobby =>
        implicit val responseType: LobbyResponseType = LobbyResponseType.LeaveLobby
        val entity = request.requestJson.parseJson.convertTo[LobbyLeave]
        if(authStatus.username.isEmpty) return unauthorized()
        val username = authStatus.username.get
        val response = lobbyService.leaveLobby(username, entity)
        resolveResponse(response)
      case LobbyRoute.SetHexMap =>
        implicit val responseType: LobbyResponseType = LobbyResponseType.SetHexMap
        val entity = request.requestJson.parseJson.convertTo[SetHexMapName]
        if(authStatus.username.isEmpty) return unauthorized()
        val username = authStatus.username.get
        val response = lobbyService.setHexmapName(username, entity)
        resolveResponse(response)
      case LobbyRoute.SetPickType =>
        implicit val responseType: LobbyResponseType = LobbyResponseType.SetPickType
        val entity = request.requestJson.parseJson.convertTo[SetPickType]
        if(authStatus.username.isEmpty) return unauthorized()
        val username = authStatus.username.get
        val response = lobbyService.setPickType(username, entity)
        resolveResponse(response)
      case LobbyRoute.SetNumberOfBans =>
        implicit val responseType: LobbyResponseType = LobbyResponseType.SetNumberOfBans
        val entity = request.requestJson.parseJson.convertTo[SetNumberOfBans]
        if(authStatus.username.isEmpty) return unauthorized()
        val username = authStatus.username.get
        val response = lobbyService.setNumberOfBans(username, entity)
        resolveResponse(response)
      case LobbyRoute.SetNumberOfCharacters =>
        implicit val responseType: LobbyResponseType = LobbyResponseType.SetNumberOfCharacters
        val entity = request.requestJson.parseJson.convertTo[SetNumberOfCharactersPerPlayer]
        if(authStatus.username.isEmpty) return unauthorized()
        val username = authStatus.username.get
        val response = lobbyService.setNumberOfCharactersPerPlayer(username, entity)
        resolveResponse(response)
      case LobbyRoute.SetLobbyName =>
        implicit val responseType: LobbyResponseType = LobbyResponseType.SetLobbyName
        val entity = request.requestJson.parseJson.convertTo[SetLobbyName]
        if(authStatus.username.isEmpty) return unauthorized()
        val username = authStatus.username.get
        val response = lobbyService.setLobbyName(username, entity)
        resolveResponse(response)
      case LobbyRoute.SetClockConfig =>
        implicit val responseType: LobbyResponseType = LobbyResponseType.SetClockConfig
        val entity = request.requestJson.parseJson.convertTo[SetClockConfig]
        if(authStatus.username.isEmpty) return unauthorized()
        val username = authStatus.username.get
        val response = lobbyService.setClockConfig(username, entity)
        resolveResponse(response)
      case LobbyRoute.StartGame =>
        implicit val responseType: LobbyResponseType = LobbyResponseType.StartGame
        val entity = request.requestJson.parseJson.convertTo[StartGame]
        if(authStatus.username.isEmpty) return unauthorized()
        val username = authStatus.username.get
        val response = lobbyService.startGame(username, entity)
        resolveResponse(response)
    }
  }
}
