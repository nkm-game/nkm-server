package com.tosware.nkm.services

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.persistence.jdbc.query.scaladsl.JdbcReadJournal
import akka.persistence.query.PersistenceQuery
import akka.stream.scaladsl.Sink
import com.tosware.nkm.NkmTimeouts
import com.tosware.nkm.actors._
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.hex.HexMap
import com.tosware.nkm.models.lobby._
import com.tosware.nkm.models.lobby.ws.LobbyRequest._
import slick.jdbc.JdbcBackend

import scala.concurrent.Future

class LobbyService(lobbiesManagerActor: ActorRef)(
  implicit db: JdbcBackend.Database,
  system: ActorSystem,
  nkmDataService: NkmDataService,
  gameService: GameService,
) extends NkmTimeouts {
  import com.tosware.nkm.models.CommandResponse._

  def getLobbyActorOption(lobbyId: String): Option[ActorRef] = {
    import LobbiesManager.GetLobbyActorResponse
    import LobbiesManager.Query.GetLobbyActor

    aw(lobbiesManagerActor ? GetLobbyActor(lobbyId))
      .asInstanceOf[GetLobbyActorResponse]
      .lobbyActor
  }

  def getLobbyActor(lobbyId: String): ActorRef =
    getLobbyActorOption(lobbyId).get

  def getLobbyState(lobbyActor: ActorRef): Future[LobbyState] =
    (lobbyActor ? Lobby.GetState).mapTo[LobbyState]

  def getLobbyState(lobbyId: String): Future[LobbyState] =
    getLobbyState(getLobbyActor(lobbyId))

  def getGameState(lobbyId: String): Future[GameState] =
    gameService.getGameState(lobbyId)

  def createLobby(name: String, hostUserId: String): CommandResponse =
    aw(lobbiesManagerActor ? Lobby.Create(name, hostUserId)).asInstanceOf[CommandResponse]

  def isLobbyCreated(lobbyId: String): Boolean =
    getLobbyActorOption(lobbyId).isDefined


  def joinLobby(userId: String, request: LobbyJoin): CommandResponse = {
    if(!isLobbyCreated(request.lobbyId)) return Failure("Lobby is not created at this id")

    val lobbyActor = getLobbyActor(request.lobbyId)
    val gameState = aw(getGameState(request.lobbyId))

    if (gameState.gameStatus != GameStatus.NotStarted) return Failure("Game is already started")

    aw(lobbyActor ? Lobby.UserJoin(userId)).asInstanceOf[CommandResponse]
  }

  def leaveLobby(userId: String, request: LobbyLeave): CommandResponse = {
    if(!isLobbyCreated(request.lobbyId)) return Failure("Lobby is not created at this id")

    val lobbyActor = getLobbyActor(request.lobbyId)
    val gameState = aw(getGameState(request.lobbyId))

    if (gameState.gameStatus != GameStatus.NotStarted) return Failure("Game is already started")

    aw(lobbyActor ? Lobby.UserLeave(userId)).asInstanceOf[CommandResponse]
  }

  def setHexmapName(username: String, request: SetHexMapName): CommandResponse = {
    if(!isLobbyCreated(request.lobbyId)) return Failure("Lobby is not created at this id")

    val lobbyActor = getLobbyActor(request.lobbyId)
    val lobbyState = aw(getLobbyState(lobbyActor))
    val gameState = aw(getGameState(request.lobbyId))
    val hexMaps = nkmDataService.getHexMaps

    if (gameState.gameStatus != GameStatus.NotStarted) return Failure("Game is already started")

    if (!lobbyState.hostUserId.contains(username)) return Failure("You are not the host")

    if (!hexMaps.map(_.name).contains(request.hexMapName)) return Failure("Hexmap with this name does not exist")

    aw(lobbyActor ? Lobby.SetMapName(request.hexMapName)).asInstanceOf[CommandResponse]
  }

  def setNumberOfCharactersPerPlayer(username: String, request: SetNumberOfCharactersPerPlayer): CommandResponse = {
    if(!isLobbyCreated(request.lobbyId)) return Failure("Lobby is not created at this id")

    val lobbyActor = getLobbyActor(request.lobbyId)
    val lobbyState = aw(getLobbyState(lobbyActor))
    val gameState = aw(getGameState(request.lobbyId))

    if (request.charactersPerPlayer < 1) return Failure("Number of characters has to be more than 0")

    if (gameState.gameStatus != GameStatus.NotStarted) return Failure("Game is already started")
    if (!lobbyState.hostUserId.contains(username)) return Failure("You are not the host")

    aw(lobbyActor ? Lobby.SetNumberOfCharactersPerPlayer(request.charactersPerPlayer)).asInstanceOf[CommandResponse]
  }

  def setNumberOfBans(username: String, request: SetNumberOfBans): CommandResponse = {
    if(!isLobbyCreated(request.lobbyId)) return Failure("Lobby is not created at this id")

    val lobbyActor = getLobbyActor(request.lobbyId)
    val lobbyState = aw(getLobbyState(lobbyActor))
    val gameState = aw(getGameState(request.lobbyId))

    if (request.numberOfBans < 0) return Failure("Number of bans has to be more or equal 0")

    if (gameState.gameStatus != GameStatus.NotStarted) return Failure("Game is already started")

    if (!lobbyState.hostUserId.contains(username)) return Failure("You are not the host")

    aw(lobbyActor ? Lobby.SetNumberOfBans(request.numberOfBans)).asInstanceOf[CommandResponse]
  }

  def setPickType(username: String, request: SetPickType): CommandResponse = {
    if(!isLobbyCreated(request.lobbyId)) return Failure("Lobby is not created at this id")

    val lobbyActor = getLobbyActor(request.lobbyId)
    val lobbyState = aw(getLobbyState(lobbyActor))
    val gameState = aw(getGameState(request.lobbyId))

    if (gameState.gameStatus != GameStatus.NotStarted) return Failure("Game is already started")

    if (!lobbyState.hostUserId.contains(username)) return Failure("You are not the host")

    aw(lobbyActor ? Lobby.SetPickType(request.pickType)).asInstanceOf[CommandResponse]
  }

  def setLobbyName(username: String, request: SetLobbyName): CommandResponse = {
    if(!isLobbyCreated(request.lobbyId)) return Failure("Lobby is not created at this id")

    val lobbyActor = getLobbyActor(request.lobbyId)
    val lobbyState = aw(getLobbyState(lobbyActor))
    val gameState = aw(getGameState(request.lobbyId))

    if (gameState.gameStatus != GameStatus.NotStarted) return Failure("Game is already started")

    if (!lobbyState.hostUserId.contains(username)) return Failure("You are not the host")

    aw(lobbyActor ? Lobby.SetLobbyName(request.newName)).asInstanceOf[CommandResponse]
  }

  def setClockConfig(username: String, request: SetClockConfig): CommandResponse = {
    if(!isLobbyCreated(request.lobbyId)) return Failure("Lobby is not created at this id")

    val lobbyActor = getLobbyActor(request.lobbyId)
    val lobbyState = aw(getLobbyState(lobbyActor))
    val gameState = aw(getGameState(request.lobbyId))

    if (gameState.gameStatus != GameStatus.NotStarted) return Failure("Game is already started")

    if (!lobbyState.hostUserId.contains(username)) return Failure("You are not the host")

    aw(lobbyActor ? Lobby.SetClockConfig(request.newConfig)).asInstanceOf[CommandResponse]
  }


  def startGame(username: String, request: StartGame): CommandResponse = {
    if(!isLobbyCreated(request.lobbyId)) return Failure("Lobby is not created at this id")

    val lobbyActor = getLobbyActor(request.lobbyId)
    val lobbyState = aw(getLobbyState(lobbyActor))
    val gameState = aw(getGameState(request.lobbyId))

    if (!lobbyState.hostUserId.contains(username)) return Failure("You are not the host")
    if (lobbyState.chosenHexMapName.isEmpty) return Failure("Chosen hex map name is empty")
    if (lobbyState.userIds.length < 2) return Failure("There are less than 2 players")

    val chosenHexMap: HexMap = nkmDataService.getHexMaps.find(_.name == lobbyState.chosenHexMapName.get).get
    if(chosenHexMap.maxNumberOfCharacters < lobbyState.userIds.length) return Failure("There are more players than allowed for this map")

    if (gameState.gameStatus != GameStatus.NotStarted) return Failure("Game is already started")

    aw(lobbyActor ? Lobby.StartGame(gameService.getGameActor(request.lobbyId))).asInstanceOf[CommandResponse]
  }

  def getAllLobbies(): Future[Seq[LobbyState]] = {
    // TODO: use lobbies manager
    val readJournal = PersistenceQuery(system).readJournalFor[JdbcReadJournal](JdbcReadJournal.Identifier)
    val lobbyIdsFuture = readJournal.
      currentEventsByTag("lobby", 0)
      .map(_.event.asInstanceOf[Lobby.CreateSuccess])
      .map(_.id)
      .runWith(Sink.seq[String])

    val lobbyIds = aw(lobbyIdsFuture)

    val lobbyActors = lobbyIds.map(lobbyId => system.actorOf(Lobby.props(lobbyId)))
    implicit val ec: scala.concurrent.ExecutionContext = system.dispatcher
    Future.sequence(lobbyActors.map(a => (a ? Lobby.GetState).map(_.asInstanceOf[LobbyState])))
  }
}
