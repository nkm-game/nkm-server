package com.tosware.NKM.actors

import akka.actor.{Actor, ActorRef, Props, Terminated}

object LobbySessionActor {
  case object Join
  case class BroadcastMessage(message: String)

  def props(): Props = Props(new LobbySessionActor)

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
    case msg: BroadcastMessage =>
      users.foreach(_ ! msg)
  }
}
