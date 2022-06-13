package com.tosware.NKM.actors.ws

import akka.actor.{Actor, ActorLogging, ActorRef, Terminated}
import com.tosware.NKM.NKMTimeouts
import com.tosware.NKM.serializers.NKMJsonProtocol

import scala.collection.mutable

object SessionActor {
  case class Observe(lobbyId: String, websocketUserOutputActor: ActorRef)
  case class Authenticate(username: String, websocketUserOutputActor: ActorRef)
}


trait SessionActor
  extends Actor
  with ActorLogging
  with NKMTimeouts
  with NKMJsonProtocol
{
  import SessionActor._

  // user can observe only one lobby at once
  private val lobbyIdByObserver = mutable.Map.empty[ActorRef, String]
  private val authStatusByObserver = mutable.Map.empty[ActorRef, Option[String]]
  private def observersBylobbyId() = lobbyIdByObserver.groupMap(_._2)(_._1)

  private def observe(lobbyId: String, websocketUserOutputActor: ActorRef): Unit =
    lobbyIdByObserver(websocketUserOutputActor) = lobbyId

  private def stopObserving(websocketUserOutputActor: ActorRef): Unit =
    lobbyIdByObserver.remove(websocketUserOutputActor)

  private def authenticate(username: String, websocketUserOutputActor: ActorRef): Unit =
    authStatusByObserver(websocketUserOutputActor) = Some(username)

  def getObservers(lobbyId: String): Set[ActorRef] =
    observersBylobbyId().getOrElse(lobbyId, Set.empty).toSet

  def getAuthStatus(websocketUserOutputActor: ActorRef): Option[String] =
    authStatusByObserver.get(websocketUserOutputActor).flatten

  def receive: Receive = {
    case Observe(lobbyId, userActor) =>
      observe(lobbyId, userActor)
      context.watch(sender())
    case Authenticate(username, userActor) =>
      authenticate(username, userActor)
    case Terminated(user) =>
      stopObserving(user)
  }
}
