package com.tosware.NKM.actors

import akka.actor.{Actor, ActorRef, Props}

object WebsocketUser {
  case class Connected(outgoing: ActorRef)
  case class IncomingMessage(text: String)
  case class OutgoingMessage(text: String)
  def props(lobbySession: ActorRef): Props = Props(new WebsocketUser(lobbySession))
}

class WebsocketUser(lobbySession: ActorRef) extends Actor {
  import WebsocketUser._

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
