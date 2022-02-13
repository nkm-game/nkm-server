package com.tosware.NKM.services.http.routes

import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.http.scaladsl.server.Directives._
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.tosware.NKM.NKMTimeouts
import com.tosware.NKM.models.lobby.{GetLobbyRequest, LobbyCreationRequest, LobbyRequest}
import com.tosware.NKM.serializers.NKMJsonProtocol
import com.tosware.NKM.services.LobbyService
import com.tosware.NKM.services.http.directives.JwtDirective
import spray.json._

import scala.concurrent.Await


case class WebsocketLobbyRequest(requestPath: LobbyRoute, requestJson: String)
case class WebsocketLobbyResponse(statusCode: Int, body: String = "")

trait WebsocketRoutes extends JwtDirective
  with SprayJsonSupport
  with NKMJsonProtocol
  with NKMTimeouts
{
  implicit val system: ActorSystem
  implicit val lobbyService: LobbyService

  def greeter = Flow[Message].mapConcat {
    case tm: TextMessage =>
      TextMessage(Source.single("Hello ") ++ tm.textStream ++ Source.single("!")) :: Nil
    case bm: BinaryMessage =>
      // ignore binary messages but drain content to avoid the stream being clogged
      bm.dataStream.runWith(Sink.ignore)
      Nil
  }

  def lobby = Flow[Message].collect {
    case TextMessage.Strict(text) =>
      val request = text.parseJson.convertTo[WebsocketLobbyRequest]
      request.requestPath match {
        case LobbyRoute.Lobbies =>
          val lobbies = Await.result(lobbyService.getAllLobbies(), atMost)
          TextMessage.Strict(lobbies.toJson.toString)
        case LobbyRoute.Lobby =>
          val lobbyId = request.requestJson.parseJson.convertTo[GetLobbyRequest].lobbyId
          val lobby = Await.result(lobbyService.getLobby(lobbyId), atMost)
          TextMessage.Strict(lobby.toJson.toString)
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

  val websocketRoutes = concat (
    path("greeter") {
      handleWebSocketMessages(greeter)
    },
    path("lobby") {
      handleWebSocketMessages(lobby)
    },
  )
}
