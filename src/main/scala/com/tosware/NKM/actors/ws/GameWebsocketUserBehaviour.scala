package com.tosware.NKM.actors.ws

import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCodes
import com.tosware.NKM.models.game.ws._
import com.tosware.NKM.models.lobby.ws.{GetLobbyRequest, LobbyResponseType, WebsocketLobbyResponse}
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

  def ok()(implicit responseType: GameResponseType): WebsocketGameResponse =
    WebsocketGameResponse(responseType, StatusCodes.OK.intValue)

  def nok(msg: String = "")(implicit responseType: GameResponseType): WebsocketGameResponse =
    WebsocketGameResponse(responseType, StatusCodes.InternalServerError.intValue, msg)

  def unauthorized()(implicit responseType: GameResponseType): WebsocketGameResponse =
    WebsocketGameResponse(responseType, StatusCodes.Unauthorized.intValue)

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
        if(authStatus.username.isEmpty) unauthorized()
        val username = authStatus.username.get
        val surrenderStatus = Await.result(gameService.surrender(username, gameId), atMost)
        surrenderStatus match {
          case GameService.Success(_) => ok()
          case GameService.Failure(msg) => nok(msg)
        }
      case GameRoute.BanCharacters => ???
      case GameRoute.PickCharacters => ???
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
