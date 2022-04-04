package com.tosware.NKM.actors.ws

import akka.actor.{Actor, ActorLogging, ActorRef, Terminated}
import com.tosware.NKM.NKMTimeouts
import com.tosware.NKM.serializers.NKMJsonProtocol

import scala.collection.mutable

object SessionActor {
  case class Observe(sessionId: String, websocketUserOutput: ActorRef)
}


trait SessionActor
  extends Actor
  with ActorLogging
  with NKMTimeouts
  with NKMJsonProtocol
{
  import SessionActor._

  // user can observe only one lobby at once
  private val sessionIdByObserver = mutable.Map.empty[ActorRef, String]
  private def observersBySessionId() = sessionIdByObserver.groupMap(_._2)(_._1)

  private def observe(sessionId: String, user: ActorRef): Unit =
    sessionIdByObserver(user) = sessionId

  private def stopObserving(user: ActorRef): Unit =
    sessionIdByObserver.remove(user)

  def getObservers(sessionId: String): Set[ActorRef] =
    observersBySessionId().getOrElse(sessionId, Set.empty).toSet

  def receive: Receive = {
    case Observe(sessionId, user) =>
      observe(sessionId, user)
      context.watch(sender())
    case Terminated(user) =>
      stopObserving(user)
  }
}
