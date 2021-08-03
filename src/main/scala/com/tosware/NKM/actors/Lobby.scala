package com.tosware.NKM.actors

import akka.actor.{ActorLogging, Props}
import akka.persistence.{PersistentActor, RecoveryCompleted}
import com.tosware.NKM.models.lobby.LobbyState

import java.time.LocalDate


object Lobby {
  sealed trait Query
  case object GetState extends Query

  sealed trait Command
  case class Create(name: String, hostUserId: String) extends Command
  case class UserJoin(userId: String) extends Command
  case class UserLeave(userId: String) extends Command
  case class SetMapName(hexMapName: String) extends Command

  sealed trait Event

  case class CreateSuccess(id: String, name: String, hostUserId: String, creationDate: LocalDate) extends Event
  case class UserJoined(id: String, userId: String) extends Event
  case class UserLeft(id: String, userId: String) extends Event
  case class MapNameSet(id: String, hexMapName: String) extends Event

  sealed trait CommandResponse
  case object Success extends CommandResponse
  case object Failure extends CommandResponse

  def props(id: String): Props = Props(new Lobby(id))
}

class Lobby(id: String) extends PersistentActor with ActorLogging {
  import Lobby._
  override def persistenceId: String = s"lobby-$id"

  var lobbyState: LobbyState = LobbyState(id)

  def create(name: String, hostUserId: String, creationDate: LocalDate): Unit =
    lobbyState = lobbyState.copy(name = Some(name), creationDate = Some(creationDate), hostUserId = Some(hostUserId), userIds = List(hostUserId))

  def joinLobby(userId: String): Unit =
    lobbyState = lobbyState.copy(userIds = lobbyState.userIds :+ userId)

  def leaveLobby(userId: String): Unit =
    lobbyState = lobbyState.copy(userIds = lobbyState.userIds.filterNot(_ == userId))

  def setMapName(hexMapName: String): Unit =
    lobbyState = lobbyState.copy(chosenHexMapName = Some(hexMapName))

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
            sender() ! Success
          }
        }
        else {
          sender() ! Failure
        }

    case UserJoin(userId: String) =>
      if(lobbyState.created() && !lobbyState.userIds.contains(userId)) {
        val userJoinedEvent = UserJoined(id, userId)
        persist(userJoinedEvent) { _ =>
          context.system.eventStream.publish(userJoinedEvent)
          joinLobby(userId)
          log.info(s"$userId joined lobby")
          sender() ! Success
        }
      } else {
        sender() ! Failure
      }

    case UserLeave(userId: String) =>
      if(lobbyState.created() && lobbyState.userIds.contains(userId)) {
        val userLeftEvent = UserLeft(id, userId)
        persist(userLeftEvent) { _ =>
          context.system.eventStream.publish(userLeftEvent)
          leaveLobby(userId)
          log.info(s"$userId left the lobby")
          sender() ! Success
        }
      } else {
        sender() ! Failure
      }


    case SetMapName(hexMapName: String) =>
      if(lobbyState.created()) {
        val mapNameSetEvent = MapNameSet(id, hexMapName)
        persist(mapNameSetEvent) { _ =>
          context.system.eventStream.publish(mapNameSetEvent)
          setMapName(hexMapName)
          log.info(s"Set map name: $hexMapName")
          sender() ! Success
        }
      } else {
        sender() ! Failure
      }
    case e => log.warning(s"Unknown message: $e")
  }
//
  override def receiveRecover: Receive = {
    case CreateSuccess(_, name, hostUserId, creationDate) =>
      create(name, hostUserId, creationDate)
      log.info(s"Recovered create")
    case UserJoined(id, userId) =>
      joinLobby(userId)
      log.info(s"Recovered user join")
    case UserLeft(id, userId) =>
      leaveLobby(userId)
      log.info(s"Recovered user leave")
    case MapNameSet(id, hexMapName) =>
      setMapName(hexMapName)
      log.info(s"Recovered setting hex map name")
    case RecoveryCompleted =>
    case e => log.warning(s"Unknown message: $e")
  }

  override def receiveCommand: Receive = {
    case _ =>
  }
}