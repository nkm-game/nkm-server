package com.tosware.nkm.actors

import akka.actor.{ActorRef, Props}
import akka.pattern.ask
import akka.persistence.journal.Tagged
import akka.persistence.{PersistentActor, RecoveryCompleted}
import com.tosware.nkm.*
import com.tosware.nkm.models.CommandResponse.*
import com.tosware.nkm.models.NkmColor
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.pick.PickType
import com.tosware.nkm.models.lobby.LobbyState
import com.tosware.nkm.services.{NkmDataService, UserService}

import java.time.ZonedDateTime

object Lobby {
  sealed trait Query
  case object GetState extends Query

  sealed trait Command
  case class Create(name: String, hostUserId: UserId) extends Command
  case class UserJoin(userId: UserId) extends Command
  case class UserLeave(userId: UserId) extends Command
  case class SetMapName(hexMapName: String) extends Command
  case class SetNumberOfBans(numberOfBans: Int) extends Command
  case class SetNumberOfCharactersPerPlayer(numberOfCharactersPerPlayer: Int) extends Command
  case class SetPickType(pickType: PickType) extends Command
  case class SetLobbyName(name: String) extends Command
  case class SetClockConfig(clockConfig: ClockConfig) extends Command
  case class SetColor(userId: UserId, newColorName: String) extends Command
  case class StartGame(gameActor: ActorRef) extends Command

  sealed trait Event {
    val id: GameId
  }

  case class CreateSuccess(
      id: GameId,
      name: String,
      hostUserId: UserId,
      creationDate: ZonedDateTime,
      preferredColorOpt: Option[NkmColor],
  ) extends Event
  case class UserJoined(id: GameId, userId: UserId, preferredColorOpt: Option[NkmColor]) extends Event
  case class UserLeft(id: GameId, userId: UserId) extends Event
  case class MapNameSet(id: GameId, hexMapName: String) extends Event
  case class NumberOfBansSet(id: GameId, numberOfBans: Int) extends Event
  case class NumberOfCharactersPerPlayerSet(id: GameId, numberOfCharactersPerPlayer: Int) extends Event
  case class PickTypeSet(id: GameId, pickType: PickType) extends Event
  case class LobbyNameSet(id: GameId, name: String) extends Event
  case class ClockConfigSet(id: GameId, clockConfig: ClockConfig) extends Event
  case class ColorSet(id: GameId, userId: UserId, newColor: NkmColor) extends Event
  case class GameStarted(id: GameId) extends Event

  def props(id: GameId)(implicit nkmDataService: NkmDataService, userService: UserService): Props = Props(new Lobby(id))

  object UseCheck {
    def IsNotCreated(lobbyState: LobbyState): UseCheck =
      (!lobbyState.created()) -> "Lobby is already created"
    def IsCreated(lobbyState: LobbyState): UseCheck =
      lobbyState.created() -> "Lobby is not created"
    def IsUserNotInLobby(userId: UserId)(lobbyState: LobbyState): UseCheck =
      (!lobbyState.userIds.contains(userId)) -> s"Lobby already contains $userId"
    def IsUserInLobby(userId: UserId)(lobbyState: LobbyState): UseCheck =
      lobbyState.userIds.contains(userId) -> s"Lobby does not contain $userId"
    def IsValidMapName(hexMapName: String)(implicit nkmDataService: NkmDataService): UseCheck =
      nkmDataService.getHexMaps.map(_.name).contains(hexMapName) -> s"Invalid map name: $hexMapName"
    def IsValidNumberOfBans(numberOfBans: Int, minBans: Int, maxBans: Int): UseCheck =
      (numberOfBans >= minBans && numberOfBans <= maxBans) -> s"Invalid number of bans: $numberOfBans"
    def IsValidNumberOfCharacters(numberOfCharacters: Int, minCharacters: Int, maxCharacters: Int): UseCheck =
      (numberOfCharacters >= minCharacters && numberOfCharacters <= maxCharacters) -> s"Invalid number of characters per player: $numberOfCharacters"
    def CanStartGame(lobbyState: LobbyState): UseCheck =
      (lobbyState.chosenHexMapName.nonEmpty && lobbyState.userIds.length > 1) -> "Cannot start the game"
    def IsColorNameUnique(newColorName: String)(lobbyState: LobbyState): UseCheck =
      (!lobbyState.playerColors.values.map(_.name).toSet.contains(newColorName)) -> "Color name already taken"
    def ColorExists(newColorName: String): UseCheck =
      NkmColor.colorByName(newColorName).nonEmpty -> "Color does not exist"
  }
}

class Lobby(id: GameId)(implicit nkmDataService: NkmDataService, userService: UserService)
    extends PersistentActor
    with Logging
    with NkmTimeouts {

  import Lobby.*

  override def persistenceId: String = s"lobby-$id"

  var lobbyState: LobbyState = LobbyState(id)

  def create(name: String, hostUserId: UserId, creationDate: ZonedDateTime, preferredColorOpt: Option[NkmColor]): Unit =
    lobbyState = lobbyState.copy(
      name = Some(name),
      creationDate = Some(creationDate),
      hostUserId = Some(hostUserId),
      userIds = List(hostUserId),
      playerColors = Map(hostUserId -> preferredColorOpt.getOrElse(NkmColor.availableColors.head)),
    )

  def joinLobby(userId: UserId, preferredColorOpt: Option[NkmColor]): Unit = {
    lobbyState = lobbyState.copy(userIds = lobbyState.userIds :+ userId)
    val takenColors = lobbyState.playerColors.values.toSet
    val nextColor = NkmColor.availableColors.filterNot(c => takenColors.contains(c)).head

    preferredColorOpt match {
      case Some(preferredColor) =>
        if (takenColors.contains(preferredColor)) {
          setColor(userId, nextColor)
        } else {
          setColor(userId, preferredColor)
        }
      case None =>
        setColor(userId, nextColor)
    }
  }

  def leaveLobby(userId: UserId): Unit =
    lobbyState = lobbyState.copy(
      userIds = lobbyState.userIds.filterNot(_ == userId),
      playerColors = lobbyState.playerColors.removed(userId),
    )

  def setMapName(hexMapName: String): Unit =
    lobbyState = lobbyState.copy(chosenHexMapName = Some(hexMapName))

  def setNumberOfBans(numberOfBans: Int): Unit =
    lobbyState = lobbyState.copy(numberOfBans = numberOfBans)

  def setNumberOfCharactersPerPlayer(numberOfCharacters: Int): Unit =
    lobbyState = lobbyState.copy(numberOfCharactersPerPlayer = numberOfCharacters)

  def setPickType(pickType: PickType): Unit =
    lobbyState = lobbyState.copy(pickType = pickType)
      .copy(clockConfig = ClockConfig.defaultForPickType(pickType))

  def setLobbyName(name: String): Unit =
    lobbyState = lobbyState.copy(name = Some(name))

  def setClockConfig(clockConfig: ClockConfig): Unit =
    lobbyState = lobbyState.copy(clockConfig = clockConfig)

  def setColor(userId: UserId, color: NkmColor): Unit =
    lobbyState = lobbyState.copy(playerColors = lobbyState.playerColors.updated(userId, color))

  def setGameStarted(): Unit =
    lobbyState = lobbyState.copy(gameStarted = true)

  def persistAndPublish[A](event: A)(handler: A => Unit): Unit = {
    context.system.eventStream.publish(event)
    persist(event)(handler)
  }

  def persistAndPublishWithTag[A](event: A, tag: String)(handler: Tagged => Unit): Unit = {
    context.system.eventStream.publish(event)
    persist(Tagged(event, Set(tag)))(handler)
  }

  def check(useChecks: Set[UseCheck])(onSuccess: => Unit): Unit =
    models.UseCheck.canBeUsed(useChecks) match {
      case Success(_) => onSuccess
      case Failure(msg) =>
        sender() ! Failure(msg)
    }

  override def receive: Receive = {
    case message =>
      Logging.withLobbyContext(id) {
        message match {
          case GetState =>
            log.debug("Received state request")
            sender() ! lobbyState
          case Create(name, hostUserId) =>
            log.debug(s"Received create request")
            val useChecks = Set(
              UseCheck.IsNotCreated(lobbyState)
            )
            check(useChecks) {
              val preferredColorOpt = userService.getUserSettings(hostUserId).preferredColor
              val creationDate = ZonedDateTime.now()
              val e = CreateSuccess(id, name, hostUserId, creationDate, preferredColorOpt)
              persistAndPublishWithTag(e, "lobby") { _ =>
                create(name, hostUserId, creationDate, preferredColorOpt)
                log.info(s"Created lobby $name for $hostUserId")
                sender() ! Success()
              }
            }

          case UserJoin(userId) =>
            log.debug(s"$userId tries to join lobby")
            val useChecks = Set(
              UseCheck.IsCreated(lobbyState),
              UseCheck.IsUserNotInLobby(userId)(lobbyState),
            )
            check(useChecks) {
              val preferredColorOpt = userService.getUserSettings(userId).preferredColor
              val e = UserJoined(id, userId, preferredColorOpt)
              persistAndPublish(e) { _ =>
                joinLobby(userId, preferredColorOpt)
                log.info(s"$userId joined lobby")
                sender() ! Success()
              }
            }
          case UserLeave(userId) =>
            log.debug(s"$userId tries to leave lobby")
            val useChecks = Set(
              UseCheck.IsCreated(lobbyState),
              UseCheck.IsUserInLobby(userId)(lobbyState),
            )
            check(useChecks) {
              val e = UserLeft(id, userId)
              persistAndPublish(e) { _ =>
                leaveLobby(userId)
                log.info(s"$userId left the lobby")
                sender() ! Success()
              }
            }
          case SetMapName(hexMapName) =>
            val useChecks = Set(
              UseCheck.IsCreated(lobbyState),
              UseCheck.IsValidMapName(hexMapName),
            )
            check(useChecks) {
              val e = MapNameSet(id, hexMapName)
              persistAndPublish(e) { _ =>
                setMapName(hexMapName)
                log.info(s"Set map name: $hexMapName")
                sender() ! Success()
              }
            }
          case SetNumberOfBans(numberOfBans) =>
            val useChecks = Set(
              UseCheck.IsCreated(lobbyState)
              // TODO(NKM-291): check based on number of available and picked characters
//        UseCheck.IsValidNumberOfBans(numberOfBans, 0, ???)
            )
            check(useChecks) {
              val e = NumberOfBansSet(id, numberOfBans)
              persistAndPublish(e) { _ =>
                setNumberOfBans(numberOfBans)
                log.info(s"Set number of bans: $numberOfBans")
                sender() ! Success()
              }
            }
          case SetNumberOfCharactersPerPlayer(numberOfCharactersPerPlayer) =>
            val useChecks = Set(
              UseCheck.IsCreated(lobbyState)
              // TODO(NKM-291): check based on the map size
//        UseCheck.IsValidNumberOfCharacters(numberOfCharactersPerPlayer, 1, ???),
            )
            check(useChecks) {
              val e = NumberOfCharactersPerPlayerSet(id, numberOfCharactersPerPlayer)
              persistAndPublish(e) { _ =>
                setNumberOfCharactersPerPlayer(numberOfCharactersPerPlayer)
                log.info(s"Set number of characters: $numberOfCharactersPerPlayer")
                sender() ! Success()
              }
            }
          case SetPickType(pickType) =>
            val useChecks = Set(
              UseCheck.IsCreated(lobbyState)
            )
            check(useChecks) {
              val e = PickTypeSet(id, pickType)
              persistAndPublish(e) { _ =>
                setPickType(pickType)
                log.info(s"Set pick type: $pickType")
                sender() ! Success()
              }
            }
          case SetLobbyName(name) =>
            val useChecks = Set(
              UseCheck.IsCreated(lobbyState)
            )
            check(useChecks) {
              val e = LobbyNameSet(id, name)
              persistAndPublish(e) { _ =>
                setLobbyName(name)
                log.info(s"Set lobby name: $name")
                sender() ! Success()
              }
            }
          case SetClockConfig(clockConfig) =>
            val useChecks = Set(
              UseCheck.IsCreated(lobbyState)
            )
            check(useChecks) {
              val e = ClockConfigSet(id, clockConfig)
              persistAndPublish(e) { _ =>
                setClockConfig(clockConfig)
                log.info(s"Set clock config: $clockConfig")
                sender() ! Success()
              }
            }
          case SetColor(userId: UserId, newColorName: String) =>
            val useChecks = Set(
              UseCheck.IsCreated(lobbyState),
              UseCheck.IsUserInLobby(userId)(lobbyState),
              UseCheck.IsColorNameUnique(newColorName)(lobbyState),
              UseCheck.ColorExists(newColorName),
            )
            check(useChecks) {
              val color = NkmColor.colorByName(newColorName).get
              val e = ColorSet(id, userId, color)
              persistAndPublish(e) { _ =>
                setColor(userId, color)
                sender() ! Success()
              }
            }
          case StartGame(gameActor) =>
            val useChecks = Set(
              UseCheck.IsCreated(lobbyState),
              UseCheck.CanStartGame(lobbyState),
            )
            check(useChecks) {
              val e = GameStarted(id)
              persistAndPublish(e) { _ =>
                val hexMaps = nkmDataService.getHexMaps
                log.info("Received game start request")
                val hostUserId = lobbyState.hostUserId.get
                val players: Seq[Player] = lobbyState.userIds.map(i => Player(i)).map {
                  case p: Player if p.name == hostUserId => p.copy(isHost = true)
                  case p: Player                         => p
                }
                val clockConfig: ClockConfig =
                  if (lobbyState.clockConfig == ClockConfig.empty())
                    ClockConfig.defaultForPickType(lobbyState.pickType)
                  else
                    lobbyState.clockConfig

                val chosenHexMapOpt = hexMaps
                  .find(m => lobbyState.chosenHexMapName.contains(m.name))
                  .map(_.toHexMap)

                chosenHexMapOpt match {
                  case Some(chosenHexMap) =>
                    val deps = GameStartDependencies(
                      players = players,
                      hexMap = chosenHexMap,
                      pickType = lobbyState.pickType,
                      numberOfBansPerPlayer = lobbyState.numberOfBans,
                      numberOfCharactersPerPlayer = lobbyState.numberOfCharactersPerPlayer,
                      nkmDataService.getCharacterMetadataSeq.toSet,
                      clockConfig = clockConfig,
                    )
                    sender() ! aw(gameActor ? Game.StartGame(deps)).asInstanceOf[CommandResponse]
                    setGameStarted()
                  case None =>
                    log.error("Map not found.")
                    sender() ! Failure("Map not found.")
                }
              }
            }

          case e => log.warn(s"Unknown message: $e")
        }
      }
  }

  override def receiveRecover: Receive = {
    case CreateSuccess(_, name, hostUserId, creationDate, preferredColorOpt) =>
      create(name, hostUserId, creationDate, preferredColorOpt)
      log.debug(s"Recovered create")
    case UserJoined(_, userId, preferredColorOpt) =>
      joinLobby(userId, preferredColorOpt)
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
    case ClockConfigSet(_, clockConfig) =>
      setClockConfig(clockConfig)
      log.debug(s"Recovered setting clock config")
    case ColorSet(_, userId, newColor) =>
      setColor(userId, newColor)
    case GameStarted(_) =>
      setGameStarted()
      log.debug(s"Recovered starting game")
    case RecoveryCompleted =>
    case e                 => log.warn(s"Unknown message: $e")
  }

  override def receiveCommand: Receive = {
    case _ =>
  }
}
