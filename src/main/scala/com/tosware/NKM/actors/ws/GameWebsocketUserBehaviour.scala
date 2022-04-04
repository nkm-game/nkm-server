package com.tosware.NKM.actors.ws

import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCodes
import com.tosware.NKM.models.game.ws.{GameResponseType, WebsocketGameRequest, WebsocketGameResponse}
import com.tosware.NKM.services.GameService
import com.tosware.NKM.services.http.directives.JwtSecretKey
import spray.json._

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

  def parseWebsocketGameRequest(request: WebsocketGameRequest, outgoing: ActorRef, self: ActorRef, status: AuthStatus): WebsocketGameResponse = ???

}
