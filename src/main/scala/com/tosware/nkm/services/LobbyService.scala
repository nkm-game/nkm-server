package com.tosware.nkm.services

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.persistence.jdbc.query.scaladsl.JdbcReadJournal
import akka.persistence.query.PersistenceQuery
import akka.stream.scaladsl.Sink
import com.tosware.nkm.NkmTimeouts
import com.tosware.nkm.actors.*
import com.tosware.nkm.actors.GameIdTrackerActor.Response
import com.tosware.nkm.models.CommandResponse
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.hex.HexMap
import com.tosware.nkm.models.lobby.*
import com.tosware.nkm.models.lobby.ws.LobbyRequest.*

import scala.concurrent.Future

class LobbyService(gameIdTrackerActor: ActorRef)(
    implicit
    system: ActorSystem,
    nkmDataService: NkmDataService,
    gameService: GameService,
) extends NkmTimeouts {
  import com.tosware.nkm.models.CommandResponse.*

  private def failGameIdDoesNotExist: Failure =
    CommandResponse.Failure("Game ID does not exist.")

  def getLobbyActorOpt(lobbyId: String): Option[ActorRef] =
    aw(gameIdTrackerActor ? GameIdTrackerActor.Query.GetLobbyActor(lobbyId))
      .asInstanceOf[GameIdTrackerActor.Response] match {
      case Response.GetLobbyActorResponse(lobbyActor) => Some(lobbyActor)
      case Response.GameIdDoesNotExist                => None
      case _                                          => None
    }

  def getLobbyState(lobbyActor: ActorRef): Future[LobbyState] =
    (lobbyActor ? Lobby.GetState).mapTo[LobbyState]

  def getLobbyStateOpt(lobbyId: String): Option[Future[LobbyState]] =
    getLobbyActorOpt(lobbyId).map(getLobbyState)

  def getGameState(lobbyId: String): Option[Future[GameState]] =
    gameService.getGameState(lobbyId)

  def createLobby(name: String, hostUserId: String): Future[CommandResponse] =
    (gameIdTrackerActor ? Lobby.Create(name, hostUserId)).mapTo[CommandResponse]

  def isLobbyCreated(lobbyId: String): Boolean =
    getLobbyActorOpt(lobbyId).isDefined

  def joinLobby(userId: String, request: LobbyJoin): CommandResponse = {
    val lobbyActor = getLobbyActorOpt(request.lobbyId).getOrElse(return failGameIdDoesNotExist)
    val gameState = aw(getGameState(request.lobbyId).getOrElse(return failGameIdDoesNotExist))

    if (gameState.gameStatus != GameStatus.NotStarted) return Failure("Game is already started")

    aw(lobbyActor ? Lobby.UserJoin(userId)).asInstanceOf[CommandResponse]
  }

  def leaveLobby(userId: String, request: LobbyLeave): CommandResponse = {
    val lobbyActor = getLobbyActorOpt(request.lobbyId).getOrElse(return failGameIdDoesNotExist)
    val gameState = aw(getGameState(request.lobbyId).getOrElse(return failGameIdDoesNotExist))

    if (gameState.gameStatus != GameStatus.NotStarted) return Failure("Game is already started")

    aw(lobbyActor ? Lobby.UserLeave(userId)).asInstanceOf[CommandResponse]
  }

  def setHexmapName(username: String, request: SetHexMapName): CommandResponse = {
    val lobbyActor = getLobbyActorOpt(request.lobbyId).getOrElse(return failGameIdDoesNotExist)
    val lobbyState = aw(getLobbyState(lobbyActor))
    val gameState = aw(getGameState(request.lobbyId).getOrElse(return failGameIdDoesNotExist))
    val hexMaps = nkmDataService.getHexMaps

    if (gameState.gameStatus != GameStatus.NotStarted) return Failure("Game is already started")

    if (!lobbyState.hostUserId.contains(username)) return Failure("You are not the host")

    if (!hexMaps.map(_.name).contains(request.hexMapName)) return Failure("Hexmap with this name does not exist")

    aw(lobbyActor ? Lobby.SetMapName(request.hexMapName)).asInstanceOf[CommandResponse]
  }

  def setNumberOfCharactersPerPlayer(username: String, request: SetNumberOfCharactersPerPlayer): CommandResponse = {
    val lobbyActor = getLobbyActorOpt(request.lobbyId).getOrElse(return failGameIdDoesNotExist)
    val lobbyState = aw(getLobbyState(lobbyActor))
    val gameState = aw(getGameState(request.lobbyId).getOrElse(return failGameIdDoesNotExist))

    if (request.charactersPerPlayer < 1) return Failure("Number of characters has to be more than 0")

    if (gameState.gameStatus != GameStatus.NotStarted) return Failure("Game is already started")
    if (!lobbyState.hostUserId.contains(username)) return Failure("You are not the host")

    aw(lobbyActor ? Lobby.SetNumberOfCharactersPerPlayer(request.charactersPerPlayer)).asInstanceOf[CommandResponse]
  }

  def setNumberOfBans(username: String, request: SetNumberOfBans): CommandResponse = {
    val lobbyActor = getLobbyActorOpt(request.lobbyId).getOrElse(return failGameIdDoesNotExist)
    val gameState = aw(getGameState(request.lobbyId).getOrElse(return failGameIdDoesNotExist))
    val lobbyState = aw(getLobbyState(lobbyActor))

    if (request.numberOfBans < 0) return Failure("Number of bans has to be more or equal 0")

    if (gameState.gameStatus != GameStatus.NotStarted) return Failure("Game is already started")

    if (!lobbyState.hostUserId.contains(username)) return Failure("You are not the host")

    aw(lobbyActor ? Lobby.SetNumberOfBans(request.numberOfBans)).asInstanceOf[CommandResponse]
  }

  def setPickType(username: String, request: SetPickType): CommandResponse = {
    val lobbyActor = getLobbyActorOpt(request.lobbyId).getOrElse(return failGameIdDoesNotExist)
    val gameState = aw(getGameState(request.lobbyId).getOrElse(return failGameIdDoesNotExist))
    val lobbyState = aw(getLobbyState(lobbyActor))

    if (gameState.gameStatus != GameStatus.NotStarted) return Failure("Game is already started")

    if (!lobbyState.hostUserId.contains(username)) return Failure("You are not the host")

    aw(lobbyActor ? Lobby.SetPickType(request.pickType)).asInstanceOf[CommandResponse]
  }

  def setLobbyName(username: String, request: SetLobbyName): CommandResponse = {
    val lobbyActor = getLobbyActorOpt(request.lobbyId).getOrElse(return failGameIdDoesNotExist)
    val gameState = aw(getGameState(request.lobbyId).getOrElse(return failGameIdDoesNotExist))
    val lobbyState = aw(getLobbyState(lobbyActor))

    if (gameState.gameStatus != GameStatus.NotStarted) return Failure("Game is already started")

    if (!lobbyState.hostUserId.contains(username)) return Failure("You are not the host")

    aw(lobbyActor ? Lobby.SetLobbyName(request.newName)).asInstanceOf[CommandResponse]
  }

  def setClockConfig(username: String, request: SetClockConfig): CommandResponse = {
    val lobbyActor = getLobbyActorOpt(request.lobbyId).getOrElse(return failGameIdDoesNotExist)
    val gameState = aw(getGameState(request.lobbyId).getOrElse(return failGameIdDoesNotExist))
    val lobbyState = aw(getLobbyState(lobbyActor))

    if (gameState.gameStatus != GameStatus.NotStarted) return Failure("Game is already started")

    if (!lobbyState.hostUserId.contains(username)) return Failure("You are not the host")

    aw(lobbyActor ? Lobby.SetClockConfig(request.newConfig)).asInstanceOf[CommandResponse]
  }

  def startGame(username: String, request: StartGame): CommandResponse = {
    val lobbyActor = getLobbyActorOpt(request.lobbyId).getOrElse(return failGameIdDoesNotExist)
    val gameState = aw(getGameState(request.lobbyId).getOrElse(return failGameIdDoesNotExist))
    val lobbyState = aw(getLobbyState(lobbyActor))

    if (!lobbyState.hostUserId.contains(username)) return Failure("You are not the host")
    if (lobbyState.chosenHexMapName.isEmpty) return Failure("Chosen hex map name is empty")
    if (lobbyState.userIds.length < 2) return Failure("There are less than 2 players")

    val chosenHexMap: HexMap = nkmDataService.getHexMaps.find(_.name == lobbyState.chosenHexMapName.get).get.toHexMap
    if (chosenHexMap.maxNumberOfPlayers < lobbyState.userIds.length)
      return Failure("There are more players than allowed for this map")

    if (gameState.gameStatus != GameStatus.NotStarted) return Failure("Game is already started")

    aw(lobbyActor ? Lobby.StartGame(gameService.getGameActorOpt(request.lobbyId).get)).asInstanceOf[CommandResponse]
  }

  def getAllLobbies(): Future[Seq[LobbyState]] = {
    // TODO: use game id tracker
    val readJournal = PersistenceQuery(system).readJournalFor[JdbcReadJournal](JdbcReadJournal.Identifier)
    val lobbyIdsFuture = readJournal.currentEventsByTag("lobby", 0)
      .map(_.event.asInstanceOf[Lobby.CreateSuccess])
      .map(_.id)
      .runWith(Sink.seq[String])

    val lobbyIds = aw(lobbyIdsFuture)

    val lobbyActors = lobbyIds.map(lobbyId => system.actorOf(Lobby.props(lobbyId)))
    implicit val ec: scala.concurrent.ExecutionContext = system.dispatcher
    Future.sequence(lobbyActors.map(a => (a ? Lobby.GetState).map(_.asInstanceOf[LobbyState])))
  }
}
