package com.tosware.nkm.actors.ws

import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCodes
import com.tosware.nkm.Logging
import com.tosware.nkm.models.CommandResponse.*
import com.tosware.nkm.models.game.ws.*
import com.tosware.nkm.services.GameService
import com.tosware.nkm.services.http.directives.JwtSecretKey
import spray.json.*

trait GameWebsocketUserBehaviour extends WebsocketUserBehaviour {
  val session: ActorRef
  implicit val gameService: GameService
  implicit val jwtSecretKey: JwtSecretKey

  import WebsocketUser.*

  override def parseIncomingMessage(outgoing: ActorRef, username: Option[String], text: String): Unit =
    Logging.withGameContext("Websocket") {
      try {
        val request = text.parseJson.convertTo[WebsocketGameRequest]

        if (request.requestPath != GameRoute.Ping) {
          log.info(s"[${username.getOrElse("")}] ${request.requestPath}")
        }
        log.debug(s"Request: $request")
        val response = parseWebsocketGameRequest(request, outgoing, self, AuthStatus(username))

        if (response.gameResponseType != GameResponseType.Ping) {
          log.debug(s"[${username.getOrElse("")}] ${response.gameResponseType}(${response.statusCode})")
        }
        val msg = s"Response: $response"
        if (response.statusCode == StatusCodes.OK.intValue) {
          log.debug(msg)
        } else {
          log.warn(msg)
        }
        outgoing ! OutgoingMessage(response.toJson.toString)
      } catch {
        case e: Exception =>
          log.error(e.toString)
          val response = WebsocketGameResponse(
            GameResponseType.Error,
            StatusCodes.InternalServerError.intValue,
            "Error with request parsing.",
          )
          outgoing ! OutgoingMessage(response.toJson.toString)
      }
    }

  def ok(msg: String = "")(implicit responseType: GameResponseType): WebsocketGameResponse =
    WebsocketGameResponse(responseType, StatusCodes.OK.intValue, msg)

  def nok(msg: String = "")(implicit responseType: GameResponseType): WebsocketGameResponse =
    WebsocketGameResponse(responseType, StatusCodes.InternalServerError.intValue, msg)

  def unauthorized(msg: String = "")(implicit responseType: GameResponseType): WebsocketGameResponse =
    WebsocketGameResponse(responseType, StatusCodes.Unauthorized.intValue, msg)

  def resolveResponse(commandResponse: CommandResponse)(implicit
      responseType: GameResponseType
  ): WebsocketGameResponse =
    commandResponse match {
      case Success(msg) => ok(msg)
      case Failure(msg) => nok(msg)
    }

  def parseWebsocketGameRequest(
      request: WebsocketGameRequest,
      outgoing: ActorRef,
      userActor: ActorRef,
      authStatus: AuthStatus,
  ): WebsocketGameResponse = {
    import GameRequest.Action.*
    import GameRequest.CharacterSelect.*
    import GameRequest.General.*
    request.requestPath match {
      case GameRoute.Ping =>
        implicit val responseType: GameResponseType = GameResponseType.Ping
        ok("pong")
      case GameRoute.Auth =>
        val token = request.requestJson.parseJson.convertTo[Auth].token
        authenticateToken(token) match {
          case Some(userStateView) =>
            userActor ! WebsocketUser.Authenticate(userStateView.email)
            session ! SessionActor.Authenticate(userStateView.email, outgoing)
            WebsocketGameResponse(GameResponseType.Auth, StatusCodes.OK.intValue, userStateView.email)
          case None =>
            WebsocketGameResponse(GameResponseType.Auth, StatusCodes.Unauthorized.intValue, "Invalid token.")
        }
      case GameRoute.Observe =>
        val lobbyId = request.requestJson.parseJson.convertTo[Observe].lobbyId
        session ! SessionActor.Observe(lobbyId, outgoing)
        WebsocketGameResponse(GameResponseType.Observe, StatusCodes.OK.intValue)
      case GameRoute.GetState =>
        val lobbyId = request.requestJson.parseJson.convertTo[GetState].lobbyId
        gameService.getGameStateViewOpt(lobbyId, authStatus.userIdOpt) match {
          case Some(gameStateViewFuture) =>
            val gameStateView = aw(gameStateViewFuture)
            WebsocketGameResponse(GameResponseType.State, StatusCodes.OK.intValue, gameStateView.toJson.toString)
          case None =>
            WebsocketGameResponse(GameResponseType.Error, StatusCodes.NotFound.intValue)
        }
      case GameRoute.GetCurrentClock =>
        val lobbyId = request.requestJson.parseJson.convertTo[GetCurrentClock].lobbyId
        gameService.getCurrentClockOpt(lobbyId) match {
          case Some(clockFuture) =>
            val clock = aw(clockFuture)
            WebsocketGameResponse(GameResponseType.GetCurrentClock, StatusCodes.OK.intValue, clock.toJson.toString)
          case None =>
            WebsocketGameResponse(GameResponseType.Error, StatusCodes.NotFound.intValue)
        }
      case GameRoute.Pause =>
        val lobbyId = request.requestJson.parseJson.convertTo[Pause].lobbyId
        implicit val responseType: GameResponseType = GameResponseType.Pause
        if (authStatus.userIdOpt.isEmpty) return unauthorized()
        val username = authStatus.userIdOpt.get
        val response = aw(gameService.pause(username, lobbyId))
        resolveResponse(response)
      case GameRoute.Surrender =>
        val lobbyId = request.requestJson.parseJson.convertTo[Surrender].lobbyId
        implicit val responseType: GameResponseType = GameResponseType.Surrender
        if (authStatus.userIdOpt.isEmpty) return unauthorized()
        val username = authStatus.userIdOpt.get
        val response = aw(gameService.surrender(username, lobbyId))
        resolveResponse(response)
      case GameRoute.BanCharacters =>
        val banCharactersRequest = request.requestJson.parseJson.convertTo[BanCharacters]
        implicit val responseType: GameResponseType = GameResponseType.BanCharacters
        if (authStatus.userIdOpt.isEmpty) return unauthorized()
        val username = authStatus.userIdOpt.get
        val response = aw(gameService.banCharacters(username, banCharactersRequest))
        resolveResponse(response)
      case GameRoute.PickCharacter =>
        val pickCharacterRequest = request.requestJson.parseJson.convertTo[PickCharacter]
        implicit val responseType: GameResponseType = GameResponseType.PickCharacter
        if (authStatus.userIdOpt.isEmpty) return unauthorized()
        val username = authStatus.userIdOpt.get
        val response = aw(gameService.pickCharacter(username, pickCharacterRequest))
        resolveResponse(response)
      case GameRoute.BlindPickCharacters =>
        val blindPickCharacterRequest = request.requestJson.parseJson.convertTo[BlindPickCharacters]
        implicit val responseType: GameResponseType = GameResponseType.BlindPickCharacters
        if (authStatus.userIdOpt.isEmpty) return unauthorized()
        val username = authStatus.userIdOpt.get
        val response = aw(gameService.blindPickCharacter(username, blindPickCharacterRequest))
        resolveResponse(response)
      case GameRoute.PlaceCharacters =>
        val placeCharactersRequest = request.requestJson.parseJson.convertTo[PlaceCharacters]
        implicit val responseType: GameResponseType = GameResponseType.PlaceCharacters
        if (authStatus.userIdOpt.isEmpty) return unauthorized()
        val username = authStatus.userIdOpt.get
        val response = aw(gameService.placeCharacters(username, placeCharactersRequest))
        resolveResponse(response)
      case GameRoute.EndTurn =>
        val endTurnRequest = request.requestJson.parseJson.convertTo[EndTurn]
        implicit val responseType: GameResponseType = GameResponseType.EndTurn
        if (authStatus.userIdOpt.isEmpty) return unauthorized()
        val username = authStatus.userIdOpt.get
        val response = aw(gameService.endTurn(username, endTurnRequest))
        resolveResponse(response)
      case GameRoute.PassTurn =>
        val passTurnRequest = request.requestJson.parseJson.convertTo[PassTurn]
        implicit val responseType: GameResponseType = GameResponseType.PassTurn
        if (authStatus.userIdOpt.isEmpty) return unauthorized()
        val username = authStatus.userIdOpt.get
        val response = aw(gameService.passTurn(username, passTurnRequest))
        resolveResponse(response)
      case GameRoute.Move =>
        val moveCharacterRequest = request.requestJson.parseJson.convertTo[Move]
        implicit val responseType: GameResponseType = GameResponseType.Move
        if (authStatus.userIdOpt.isEmpty) return unauthorized()
        val username = authStatus.userIdOpt.get
        val response = aw(gameService.moveCharacter(username, moveCharacterRequest))
        resolveResponse(response)
      case GameRoute.BasicAttack =>
        val basicAttackCharacterRequest = request.requestJson.parseJson.convertTo[BasicAttack]
        implicit val responseType: GameResponseType = GameResponseType.BasicAttack
        if (authStatus.userIdOpt.isEmpty) return unauthorized()
        val username = authStatus.userIdOpt.get
        val response = aw(gameService.basicAttackCharacter(username, basicAttackCharacterRequest))
        resolveResponse(response)
      case GameRoute.UseAbility =>
        val useAbilityRequest = request.requestJson.parseJson.convertTo[UseAbility]
        implicit val responseType: GameResponseType = GameResponseType.UseAbility
        if (authStatus.userIdOpt.isEmpty) return unauthorized()
        val username = authStatus.userIdOpt.get
        val response = aw(gameService.useAbility(username, useAbilityRequest))
        resolveResponse(response)
      case GameRoute.SendChatMessage => ???
      case GameRoute.ExecuteCommand  => ???
    }
  }

}
