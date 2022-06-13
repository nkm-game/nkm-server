package com.tosware.NKM.actors.ws

import akka.actor.{Actor, ActorLogging, ActorRef, Terminated}
import com.tosware.NKM.NKMTimeouts
import com.tosware.NKM.serializers.NKMJsonProtocol

import scala.collection.mutable

object SessionActor {
  case class Observe(gameId: String, websocketUserOutputActor: ActorRef)
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
  private val gameIdByObserver = mutable.Map.empty[ActorRef, String]
  private val authStatusByObserver = mutable.Map.empty[ActorRef, Option[String]]
  private def observersByGameId() = gameIdByObserver.groupMap(_._2)(_._1)

  private def observe(gameId: String, websocketUserOutputActor: ActorRef): Unit =
    gameIdByObserver(websocketUserOutputActor) = gameId

  private def stopObserving(websocketUserOutputActor: ActorRef): Unit =
    gameIdByObserver.remove(websocketUserOutputActor)

  private def authenticate(username: String, websocketUserOutputActor: ActorRef): Unit =
    authStatusByObserver(websocketUserOutputActor) = Some(username)

  def getObservers(gameId: String): Set[ActorRef] =
    observersByGameId().getOrElse(gameId, Set.empty).toSet

  def getAuthStatus(websocketUserOutputActor: ActorRef): Option[String] =
    authStatusByObserver.get(websocketUserOutputActor).flatten

  def receive: Receive = {
    case Observe(gameId, userActor) =>
      observe(gameId, userActor)
      context.watch(sender())
    case Authenticate(username, userActor) =>
      authenticate(username, userActor)
    case Terminated(user) =>
      stopObserving(user)
  }
}
