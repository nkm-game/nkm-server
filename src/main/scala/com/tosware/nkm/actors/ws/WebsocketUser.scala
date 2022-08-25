package com.tosware.nkm.actors.ws

import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill, Props}
import com.tosware.nkm.services.http.directives.JwtSecretKey
import com.tosware.nkm.services.{GameService, LobbyService}

object WebsocketUser {
  case object GetAuthStatus
  case class AuthStatus(username: Option[String])
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
  import WebsocketUser._

  var username: Option[String] = None

  def parseIncomingMessage(outgoing: ActorRef, username: Option[String], text: String): Unit

  def receive = {
    case Connected(outgoing) =>
      context.become(connected(outgoing))
  }

  def connected(outgoing: ActorRef): Receive = {
    log.info(s"Connected")

    {
      case GetAuthStatus =>
        log.info("get auth status")
        sender() ! AuthStatus(username)
      case Authenticate(u) =>
        username = Some(u)
      case IncomingMessage(text) =>
        parseIncomingMessage(outgoing, username, text)
      case PoisonPill =>
        log.info(s"Disconnected")
    }
  }
}
