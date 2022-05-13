package com.tosware.NKM.actors.ws

import akka.actor.ActorRef
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
      log.info(s"Request: $request")
      val response = parseWebsocketGameRequest(request, outgoing, self, AuthStatus(username))
      log.info(s"Response: $response")
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
    request.requestPath match {
      case GameRoute.Auth =>
        val token = request.requestJson.parseJson.convertTo[AuthRequest].token
        authenticateToken(token) match {
          case Some(username) =>
            userActor ! WebsocketUser.Authenticate(username)
            WebsocketGameResponse(GameResponseType.Auth, StatusCodes.OK.intValue, username)
          case None =>
            WebsocketGameResponse(GameResponseType.Auth, StatusCodes.Unauthorized.intValue, "Invalid token.")
        }
      case GameRoute.Observe =>
        val gameId = request.requestJson.parseJson.convertTo[ObserveRequest].gameId
        session ! SessionActor.Observe(gameId, outgoing)
        WebsocketGameResponse(GameResponseType.Observe, StatusCodes.OK.intValue)
      case GameRoute.GetState =>
        val gameId = request.requestJson.parseJson.convertTo[GetStateRequest].gameId
        val gameState = Await.result(gameService.getGameState(gameId), atMost)
        WebsocketGameResponse(GameResponseType.State, StatusCodes.OK.intValue, gameState.toJson.toString)
      case GameRoute.Pause => ???
      case GameRoute.Surrender =>
        val gameId = request.requestJson.parseJson.convertTo[SurrenderRequest].gameId
        implicit val responseType: GameResponseType = GameResponseType.Surrender
        if (authStatus.username.isEmpty) return unauthorized()
        val username = authStatus.username.get
        val response = Await.result(gameService.surrender(username, gameId), atMost)
        resolveResponse(response)
      case GameRoute.BanCharacters =>
        val banCharactersRequest = request.requestJson.parseJson.convertTo[BanCharactersRequest]
        implicit val responseType: GameResponseType = GameResponseType.BanCharacters
        if (authStatus.username.isEmpty) return unauthorized()
        val username = authStatus.username.get
        val response = Await.result(gameService.banCharacters(username, banCharactersRequest), atMost)
        resolveResponse(response)
      case GameRoute.PickCharacter =>
        val pickCharacterRequest = request.requestJson.parseJson.convertTo[PickCharacterRequest]
        implicit val responseType: GameResponseType = GameResponseType.PickCharacter
        if (authStatus.username.isEmpty) return unauthorized()
        val username = authStatus.username.get
        val response = Await.result(gameService.pickCharacter(username, pickCharacterRequest), atMost)
        resolveResponse(response)
      case GameRoute.BlindPickCharacters =>
        val blindPickCharacterRequest = request.requestJson.parseJson.convertTo[BlindPickCharactersRequest]
        implicit val responseType: GameResponseType = GameResponseType.BlindPickCharacters
        if (authStatus.username.isEmpty) return unauthorized()
        val username = authStatus.username.get
        val response = Await.result(gameService.blindPickCharacter(username, blindPickCharacterRequest), atMost)
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
