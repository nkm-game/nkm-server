package com.tosware.NKM.actors

import akka.actor.{ActorLogging, Props}
import akka.persistence.{PersistentActor, RecoveryCompleted}
import com.tosware.NKM.models.game.PickType
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
  case class SetNumberOfBans(numberOfBans: Int) extends Command
  case class SetNumberOfCharactersPerPlayer(numberOfCharactersPerPlayer: Int) extends Command
  case class SetPickType(pickType: PickType) extends Command

  sealed trait Event

  case class CreateSuccess(id: String, name: String, hostUserId: String, creationDate: LocalDate) extends Event
  case class UserJoined(id: String, userId: String) extends Event
  case class UserLeft(id: String, userId: String) extends Event
  case class MapNameSet(id: String, hexMapName: String) extends Event
  case class NumberOfBansSet(id: String, numberOfBans: Int) extends Event
  case class NumberOfCharactersPerPlayerSet(id: String, numberOfCharactersPerPlayer: Int) extends Event
  case class PickTypeSet(id: String, pickType: PickType) extends Event

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

  def setNumberOfBans(numberOfBans: Int): Unit =
    lobbyState = lobbyState.copy(numberOfBans = numberOfBans)

  def setNumberOfCharactersPerPlayer(numberOfCharacters: Int): Unit =
    lobbyState = lobbyState.copy(numberOfCharactersPerPlayer = numberOfCharacters)

  def setPickType(pickType: PickType): Unit =
    lobbyState = lobbyState.copy(pickType = pickType)

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

    case SetNumberOfBans(numberOfBans) =>
      if(lobbyState.created()) {
        val numberOfBansSetEvent = NumberOfBansSet(id, numberOfBans)
        persist(numberOfBansSetEvent) { _ =>
          context.system.eventStream.publish(numberOfBansSetEvent)
          setNumberOfBans(numberOfBans)
          log.info(s"Set number of bans: $numberOfBans")
          sender() ! Success
        }
      } else {
        sender() ! Failure
      }

    case SetNumberOfCharactersPerPlayer(numberOfCharactersPerPlayer) =>
      if(lobbyState.created()) {
        val numberOfCharactersPerPlayerSetEvent  = NumberOfCharactersPerPlayerSet(id, numberOfCharactersPerPlayer)
        persist(numberOfCharactersPerPlayerSetEvent) { _ =>
          context.system.eventStream.publish(numberOfCharactersPerPlayerSetEvent)
          setNumberOfCharactersPerPlayer(numberOfCharactersPerPlayer)
          log.info(s"Set number of characters: $numberOfCharactersPerPlayer")
          sender() ! Success
        }
      } else {
        sender() ! Failure
      }

    case SetPickType(pickType) =>
      if(lobbyState.created()) {
        val pickTypeSetEvent = PickTypeSet(id, pickType)
        persist(pickTypeSetEvent) { _ =>
          context.system.eventStream.publish(pickTypeSetEvent)
          setPickType(pickType)
          log.info(s"Set pick type: $pickType")
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
    case UserJoined(_, userId) =>
      joinLobby(userId)
      log.info(s"Recovered user join")
    case UserLeft(_, userId) =>
      leaveLobby(userId)
      log.info(s"Recovered user leave")
    case MapNameSet(_, hexMapName) =>
      setMapName(hexMapName)
      log.info(s"Recovered setting hex map name")
    case NumberOfBansSet(_, numberOfBans) =>
      setNumberOfBans(numberOfBans)
      log.info(s"Recovered setting number of bans")
    case NumberOfCharactersPerPlayerSet(_, numberOfCharactersPerPlayer) =>
      setNumberOfCharactersPerPlayer(numberOfCharactersPerPlayer)
      log.info(s"Recovered setting number of characters")
    case PickTypeSet(_, pickType) =>
      setPickType(pickType)
      log.info(s"Recovered setting pick type")
    case RecoveryCompleted =>
    case e => log.warning(s"Unknown message: $e")
  }

  override def receiveCommand: Receive = {
    case _ =>
  }
}