package com.tosware.nkm.actors.ws

import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill, Props}
import com.tosware.nkm.UserId
import com.tosware.nkm.services.http.directives.JwtSecretKey
import com.tosware.nkm.services.{GameService, LobbyService}

object WebsocketUser {
  case object GetAuthStatus
  case class AuthStatus(userIdOpt: Option[UserId])
  case class Connected(outgoing: ActorRef)
  case class IncomingMessage(text: String)
  case class OutgoingMessage(text: String)
  case class Authenticate(username: String)

  def lobbyProps(session: ActorRef)(implicit lobbyService: LobbyService, jwtSecretKey: JwtSecretKey): Props = Props(new LobbyWSUser(session))
  def gameProps(session: ActorRef)(implicit gameService: GameService, jwtSecretKey: JwtSecretKey): Props = Props(new GameWSUser(session))
}

trait WebsocketUser
  extends Actor
  with ActorLogging
{
  import WebsocketUser.*

  var userId: Option[UserId] = None

  def parseIncomingMessage(outgoing: ActorRef, userId: Option[UserId], text: String): Unit

  def receive: Receive = {
    case Connected(outgoing) =>
      context.become(connected(outgoing))
  }

  private def connected(outgoing: ActorRef): Receive = {
    log.info(s"Connected")

    {
      case GetAuthStatus =>
        log.info("get auth status")
        sender() ! AuthStatus(userId)
      case Authenticate(u) =>
        userId = Some(u)
      case IncomingMessage(text) =>
        parseIncomingMessage(outgoing, userId, text)
      case PoisonPill =>
        log.info(s"Disconnected")
    }
  }
}
