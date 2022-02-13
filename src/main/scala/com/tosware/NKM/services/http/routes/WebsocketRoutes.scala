package com.tosware.NKM.services.http.routes

import akka.NotUsed
import akka.actor.{Actor, ActorRef, ActorSystem, PoisonPill, Props, Terminated}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.http.scaladsl.server.Directives._
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.tosware.NKM.NKMTimeouts
import com.tosware.NKM.models.lobby.{GetLobbyRequest, LobbyCreationRequest, LobbyRequest}
import com.tosware.NKM.serializers.NKMJsonProtocol
import com.tosware.NKM.services.LobbyService
import com.tosware.NKM.services.http.directives.JwtDirective
import spray.json._

import scala.concurrent.Await

// object to keep track of lobby sessions
//object LobbySessionMap {
//  private var sessions = Map.empty[String, LobbySession]
//
//  def findOrCreate(userId: String)(implicit system: ActorSystem) = sessions.getOrElse(userId, create(userId))
//
//  private def create(userId: String)(implicit system: ActorSystem) = {
//    val session = LobbySession(userId)
//    sessions += userId -> session
//    session
//  }
//}

object LobbySessionActor {
  case object Join
  case class ChatMessage(message: String)
}

class LobbySessionActor extends Actor {
  import LobbySessionActor._

  var users: Set[ActorRef] = Set.empty
  def receive = {
    case Join =>
      users += sender()
      context.watch(sender())
    case Terminated(user) =>
      users -= user
    case msg: ChatMessage =>
      users.foreach(_ ! msg)
  }
}

object User {
  case class Connected(outgoing: ActorRef)
  case class IncomingMessage(text: String)
  case class OutgoingMessage(text: String)
}

class User(lobbySession: ActorRef) extends Actor {

  import User._

  def receive = {
    case Connected(outgoing) =>
      context.become(connected(outgoing))
  }

  def connected(outgoing: ActorRef): Receive = {
    lobbySession ! LobbySessionActor.Join

    {
      case IncomingMessage(text) =>
        lobbySession ! LobbySessionActor.ChatMessage(text)

      case LobbySessionActor.ChatMessage(text) =>
        outgoing ! OutgoingMessage(text)
    }
  }
}



case class WebsocketLobbyRequest(requestPath: LobbyRoute, requestJson: String)
case class WebsocketLobbyResponse(statusCode: Int, body: String = "")

trait WebsocketRoutes extends JwtDirective
  with SprayJsonSupport
  with NKMJsonProtocol
  with NKMTimeouts
{
  implicit val system: ActorSystem
  implicit val lobbyService: LobbyService

  lazy val lobbySession = system.actorOf(Props(new LobbySessionActor), "lobby")

  def newUser() = {
    // new connection - new user actor
    val userActor = system.actorOf(Props(new User(lobbySession)))

    val incomingMessages =
      Flow[Message].map {
        // transform websocket message to domain message
        case TextMessage.Strict(text) => User.IncomingMessage(text)
      }.to(Sink.actorRef[User.IncomingMessage](userActor, PoisonPill))

    val outgoingMessages =
      Source.actorRef[User.OutgoingMessage](10, OverflowStrategy.fail)
        .mapMaterializedValue { outActor =>
          // give the user actor a way to send messages out
          userActor ! User.Connected(outActor)
          NotUsed
        }.map(
        // transform domain message to web socket message
        (outMsg: User.OutgoingMessage) => TextMessage(outMsg.text))

    // then combine both to a flow
    Flow.fromSinkAndSource(incomingMessages, outgoingMessages)
  }

  def greeter = Flow[Message].collect {
    case TextMessage.Strict(text) => TextMessage(s"Hello ${text}!")
  }

  def lobby = Flow[Message].collect {
    case TextMessage.Strict(text) =>
      val request = text.parseJson.convertTo[WebsocketLobbyRequest]
      request.requestPath match {
        case LobbyRoute.Lobbies =>
          val lobbies = Await.result(lobbyService.getAllLobbies(), atMost)
          TextMessage(lobbies.toJson.toString)
        case LobbyRoute.Lobby =>
          val lobbyId = request.requestJson.parseJson.convertTo[GetLobbyRequest].lobbyId
          val lobby = Await.result(lobbyService.getLobby(lobbyId), atMost)
          TextMessage(lobby.toJson.toString)
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
    path("test") {
      handleWebSocketMessages(newUser())
    },
    path("greeter") {
      handleWebSocketMessages(greeter)
    },
    path("lobby") {
      handleWebSocketMessages(lobby)
    },
  )
}
