package com.tosware.NKM.actors

import akka.actor.{ActorLogging, ActorRef, Props}
import akka.persistence.{PersistentActor, RecoveryCompleted}
import com.tosware.NKM.models.game.{GameStartDependencies, HexMap, PickType, Player}
import com.tosware.NKM.models.lobby.LobbyState
import akka.pattern.ask
import com.tosware.NKM.NKMTimeouts
import com.tosware.NKM.actors.NKMData.GetHexMaps
import com.tosware.NKM.models.CommandResponse
import com.tosware.NKM.models.CommandResponse._
import com.tosware.NKM.services.NKMDataService

import java.time.LocalDate
import scala.concurrent.Await

object Lobby {
  sealed trait Query
  case object GetState extends Query

  sealed trait Command
  case object StartGame extends Command
  case class Create(name: String, hostUserId: String) extends Command
  case class UserJoin(userId: String) extends Command
  case class UserLeave(userId: String) extends Command
  case class SetMapName(hexMapName: String) extends Command
  case class SetNumberOfBans(numberOfBans: Int) extends Command
  case class SetNumberOfCharactersPerPlayer(numberOfCharactersPerPlayer: Int) extends Command
  case class SetPickType(pickType: PickType) extends Command
  case class SetLobbyName(name: String) extends Command

  sealed trait Event {
    val id: String
  }
  case class CreateSuccess(id: String, name: String, hostUserId: String, creationDate: LocalDate) extends Event
  case class UserJoined(id: String, userId: String) extends Event
  case class UserLeft(id: String, userId: String) extends Event
  case class MapNameSet(id: String, hexMapName: String) extends Event
  case class NumberOfBansSet(id: String, numberOfBans: Int) extends Event
  case class NumberOfCharactersPerPlayerSet(id: String, numberOfCharactersPerPlayer: Int) extends Event
  case class PickTypeSet(id: String, pickType: PickType) extends Event
  case class LobbyNameSet(id: String, name: String) extends Event

  def props(id: String)(implicit NKMDataService: NKMDataService): Props = Props(new Lobby(id))
}

class Lobby(id: String)(implicit NKMDataService: NKMDataService)
  extends PersistentActor
    with ActorLogging
    with NKMTimeouts
{
  import Lobby._
  override def persistenceId: String = s"lobby-$id"

  override def log = {
    akka.event.Logging(context.system, s"${this.getClass}($persistenceId)")
  }

  var lobbyState: LobbyState = LobbyState(id)
  val gameActor: ActorRef = context.system.actorOf(Game.props(id))
  val nkmData: ActorRef = context.system.actorOf(NKMData.props())

  def canStartGame(): Boolean =
    lobbyState.chosenHexMapName.nonEmpty && lobbyState.userIds.length > 1

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

  def setLobbyName(name: String): Unit =
    lobbyState = lobbyState.copy(name = Some(name))

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
      log.info(s"$userId tries to join lobby")
      log.warning(lobbyState.toString)
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
      log.info(s"$userId tries to leave lobby")
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

    case SetLobbyName(name) =>
      if(lobbyState.created()) {
        val lobbyNameSetEvent = LobbyNameSet(id, name)
        persist(lobbyNameSetEvent) { _ =>
          context.system.eventStream.publish(lobbyNameSetEvent)
          setLobbyName(name)
          log.info(s"Set lobby name: $name")
          sender() ! Success
        }
      } else {
        sender() ! Failure
      }

    case StartGame =>
      if(canStartGame()) {
        val hexMaps = Await.result(nkmData ? GetHexMaps, atMost).asInstanceOf[List[HexMap]]
        log.info("Received game start request")
        val deps = GameStartDependencies(
          players = lobbyState.userIds.map(i => Player(i)),
          hexMap = hexMaps.filter(m => m.name == lobbyState.chosenHexMapName.get).head,
          pickType = lobbyState.pickType,
          numberOfBans = lobbyState.numberOfBans,
          numberOfCharactersPerPlayers = lobbyState.numberOfCharactersPerPlayer,
        )
        val r = Await.result(gameActor ? Game.StartGame(deps), atMost).asInstanceOf[CommandResponse]
        sender() ! r
      }
      else {
        sender() ! Failure
      }

    case e => log.warning(s"Unknown message: $e")
  }
//
  override def receiveRecover: Receive = {
    case CreateSuccess(_, name, hostUserId, creationDate) =>
      create(name, hostUserId, creationDate)
      log.debug(s"Recovered create")
    case UserJoined(_, userId) =>
      joinLobby(userId)
      log.debug(s"Recovered user join")
    case UserLeft(_, userId) =>
      leaveLobby(userId)
      log.debug(s"Recovered user leave")
    case MapNameSet(_, hexMapName) =>
      setMapName(hexMapName)
      log.debug(s"Recovered setting hex map name")
    case NumberOfBansSet(_, numberOfBans) =>
      setNumberOfBans(numberOfBans)
      log.debug(s"Recovered setting number of bans")
    case NumberOfCharactersPerPlayerSet(_, numberOfCharactersPerPlayer) =>
      setNumberOfCharactersPerPlayer(numberOfCharactersPerPlayer)
      log.debug(s"Recovered setting number of characters")
    case PickTypeSet(_, pickType) =>
      setPickType(pickType)
      log.debug(s"Recovered setting pick type")
    case LobbyNameSet(_, name) =>
      setLobbyName(name)
      log.debug(s"Recovered setting name")
    case RecoveryCompleted =>
    case e => log.warning(s"Unknown message: $e")
  }

  override def receiveCommand: Receive = {
    case _ =>
  }
}