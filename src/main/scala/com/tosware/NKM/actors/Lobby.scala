package com.tosware.NKM.actors

import akka.actor.{ActorLogging, Props}
import akka.persistence.{PersistentActor, RecoveryCompleted}
import com.tosware.NKM.models._

import java.time.LocalDate
import scala.::


object Lobby {
  sealed trait Query
  case object GetState extends Query

  sealed trait Command
  case class Create(name: String, hostUserId: String) extends Command
  case class UserJoin(userId: String) extends Command
  case class UserLeave(userId: String) extends Command

  sealed trait Event

  case class CreateSuccess(id: String, name: String, hostUserId: String, creationDate: LocalDate) extends Event
  case object CreateSuccess extends Event
  case object CreateFailure extends Event

  case class UserJoined(id: String, userId: String) extends Event
  case object JoinSuccess extends Event
  case object JoinFailure extends Event

  case class UserLeft(id: String, userId: String) extends Event
  case object LeaveSuccess extends Event
  case object LeaveFailure extends Event

  def props(id: String): Props = Props(new Lobby(id))
}

class Lobby(id: String) extends PersistentActor with ActorLogging {
  import Lobby._
  override def persistenceId: String = s"lobby-$id"

  var lobbyState: LobbyState = LobbyState(id)

  def create(name: String, hostUserId: String, creationDate: LocalDate): Unit = {
    lobbyState = lobbyState.copy(name = Some(name), creationDate = Some(creationDate), hostUserId = Some(hostUserId), userIds = List(hostUserId))
  }

  def joinLobby(userId: String): Unit = {
    lobbyState = lobbyState.copy(userIds = lobbyState.userIds :+ userId)
  }

  def leaveLobby(userId: String): Unit = {
    lobbyState = lobbyState.copy(userIds = lobbyState.userIds.filterNot(_ == userId))
  }

  override def receive: Receive = {
    case GetState =>
      log.info("Received state request")
      sender() ! lobbyState
    case Create(name, hostUserId) =>
      log.info(s"Received create request")
        if(!lobbyState.created()) {
          val creationDate = LocalDate.now()
          val createSuccessEvent = CreateSuccess(id, name, hostUserId, creationDate)
          persist(createSuccessEvent) { _ =>
            context.system.eventStream.publish(createSuccessEvent)
            create(name, hostUserId, creationDate)
            log.info(s"Created lobby $name for $hostUserId")
            sender() ! CreateSuccess
          }
        }
        else {
          sender() ! CreateFailure
        }

    case UserJoin(userId: String) =>
      if(!lobbyState.userIds.contains(userId)) {
        val userJoinedEvent = UserJoined(id, userId)
        persist(userJoinedEvent) { _ =>
          context.system.eventStream.publish(userJoinedEvent)
          joinLobby(userId)
          log.info(s"$userId joined lobby")
          sender() ! JoinSuccess
        }
      } else {
        sender() ! JoinFailure
      }

    case UserLeave(userId: String) =>
      if(lobbyState.userIds.contains(userId)) {
        val userLeftEvent = UserLeft(id, userId)
        persist(userLeftEvent) { _ =>
          context.system.eventStream.publish(userLeftEvent)
          leaveLobby(userId)
          log.info(s"$userId left the lobby")
          sender() ! LeaveSuccess
        }
      } else {
        sender() ! LeaveFailure
      }
    case e => log.warning(s"Unknown message: $e")
  }
//
  override def receiveRecover: Receive = {
    case CreateSuccess(id, name, hostUserId, creationDate) =>
      create(name, hostUserId, creationDate)
      log.info(s"Recovered create")
    case RecoveryCompleted =>
    case e => log.warning(s"Unknown message: $e")
  }

  override def receiveCommand: Receive = {
    case _ =>
  }
}