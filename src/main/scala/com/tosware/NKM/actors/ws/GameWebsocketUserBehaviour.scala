package com.tosware.NKM.actors.ws

import akka.actor.ActorRef
import akka.event.Logging.{DebugLevel, LogLevel, WarningLevel}
import akka.http.scaladsl.model.StatusCodes
import com.tosware.NKM.models.CommandResponse.{CommandResponse, Failure, Success}
import com.tosware.NKM.models.game.ws._
import com.tosware.NKM.services.GameService
import com.tosware.NKM.services.http.directives.JwtSecretKey
import spray.json._

import scala.concurrent.Await

trait GameWebsocketUserBehaviour extends WebsocketUserBehaviour {
  val session: ActorRef
  implicit val gameService: GameService
  implicit val jwtSecretKey: JwtSecretKey

  import WebsocketUser._

  override def parseIncomingMessage(outgoing: ActorRef, username: Option[String], text: String): Unit =
    try {
      val request = text.parseJson.convertTo[WebsocketGameRequest]
      log.info(s"${request.requestPath}")
      log.debug(s"Request: $request")
      val response = parseWebsocketGameRequest(request, outgoing, self, AuthStatus(username))
      log.info(s"${response.gameResponseType}(${response.statusCode})")
      val responseLogLevel = if(response.statusCode == StatusCodes.OK.intValue) DebugLevel else WarningLevel
      log.log(responseLogLevel, s"Response: $response")
      outgoing ! OutgoingMessage(response.toJson.toString)
    }
    catch {
      case e: Exception =>
        log.error(e.toString)
        val response = WebsocketGameResponse(GameResponseType.Error, StatusCodes.InternalServerError.intValue, "Error with request parsing.")
        outgoing ! OutgoingMessage(response.toJson.toString)
    }

  def ok(msg: String = "")(implicit responseType: GameResponseType): WebsocketGameResponse =
    WebsocketGameResponse(responseType, StatusCodes.OK.intValue, msg)

  def nok(msg: String = "")(implicit responseType: GameResponseType): WebsocketGameResponse =
    WebsocketGameResponse(responseType, StatusCodes.InternalServerError.intValue, msg)

  def unauthorized(msg: String = "")(implicit responseType: GameResponseType): WebsocketGameResponse =
    WebsocketGameResponse(responseType, StatusCodes.Unauthorized.intValue, msg)

  def resolveResponse(commandResponse: CommandResponse)(implicit responseType: GameResponseType): WebsocketGameResponse = {
    commandResponse match {
      case Success(msg) => ok(msg)
      case Failure(msg) => nok(msg)
    }
  }

  def parseWebsocketGameRequest(request: WebsocketGameRequest, outgoing: ActorRef, userActor: ActorRef, authStatus: AuthStatus): WebsocketGameResponse = {
    import GameRequest.General._
    import GameRequest.CharacterSelect._
    import GameRequest.Action._
    import GameRequest.Chat._
    request.requestPath match {
      case GameRoute.Auth =>
        val token = request.requestJson.parseJson.convertTo[Auth].token
        authenticateToken(token) match {
          case Some(username) =>
            userActor ! WebsocketUser.Authenticate(username)
            session ! SessionActor.Authenticate(username, outgoing)
            WebsocketGameResponse(GameResponseType.Auth, StatusCodes.OK.intValue, username)
          case None =>
            WebsocketGameResponse(GameResponseType.Auth, StatusCodes.Unauthorized.intValue, "Invalid token.")
        }
      case GameRoute.Observe =>
        val lobbyId = request.requestJson.parseJson.convertTo[Observe].lobbyId
        session ! SessionActor.Observe(lobbyId, outgoing)
        WebsocketGameResponse(GameResponseType.Observe, StatusCodes.OK.intValue)
      case GameRoute.GetState =>
        val lobbyId = request.requestJson.parseJson.convertTo[GetState].lobbyId
        val gameStateView = aw(gameService.getGameStateView(lobbyId, authStatus.username))
        WebsocketGameResponse(GameResponseType.State, StatusCodes.OK.intValue, gameStateView.toJson.toString)
      case GameRoute.Pause =>
        val lobbyId = request.requestJson.parseJson.convertTo[Pause].lobbyId
        implicit val responseType: GameResponseType = GameResponseType.Pause
        if (authStatus.username.isEmpty) return unauthorized()
        val username = authStatus.username.get
        val response = aw(gameService.pause(username, lobbyId))
        resolveResponse(response)
      case GameRoute.Surrender =>
        val lobbyId = request.requestJson.parseJson.convertTo[Surrender].lobbyId
        implicit val responseType: GameResponseType = GameResponseType.Surrender
        if (authStatus.username.isEmpty) return unauthorized()
        val username = authStatus.username.get
        val response = aw(gameService.surrender(username, lobbyId))
        resolveResponse(response)
      case GameRoute.BanCharacters =>
        val banCharactersRequest = request.requestJson.parseJson.convertTo[BanCharacters]
        implicit val responseType: GameResponseType = GameResponseType.BanCharacters
        if (authStatus.username.isEmpty) return unauthorized()
        val username = authStatus.username.get
        val response = aw(gameService.banCharacters(username, banCharactersRequest))
        resolveResponse(response)
      case GameRoute.PickCharacter =>
        val pickCharacterRequest = request.requestJson.parseJson.convertTo[PickCharacter]
        implicit val responseType: GameResponseType = GameResponseType.PickCharacter
        if (authStatus.username.isEmpty) return unauthorized()
        val username = authStatus.username.get
        val response = aw(gameService.pickCharacter(username, pickCharacterRequest))
        resolveResponse(response)
      case GameRoute.BlindPickCharacters =>
        val blindPickCharacterRequest = request.requestJson.parseJson.convertTo[BlindPickCharacters]
        implicit val responseType: GameResponseType = GameResponseType.BlindPickCharacters
        if (authStatus.username.isEmpty) return unauthorized()
        val username = authStatus.username.get
        val response = aw(gameService.blindPickCharacter(username, blindPickCharacterRequest))
        resolveResponse(response)
      case GameRoute.PlaceCharacters => ???
      case GameRoute.EndTurn => ???
      case GameRoute.Move => ???
      case GameRoute.BasicAttack => ???
      case GameRoute.UseAbility => ???
      case GameRoute.SendChatMessage => ???
      case GameRoute.ExecuteCommand => ???
    }
  }

}
