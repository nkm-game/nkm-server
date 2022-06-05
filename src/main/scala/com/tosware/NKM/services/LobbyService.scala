package com.tosware.NKM.services

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.persistence.jdbc.query.scaladsl.JdbcReadJournal
import akka.persistence.query.PersistenceQuery
import akka.stream.scaladsl.Sink
import com.tosware.NKM.NKMTimeouts
import com.tosware.NKM.actors._
import com.tosware.NKM.models.game._
import com.tosware.NKM.models.lobby._
import com.tosware.NKM.models.lobby.ws._
import slick.jdbc.JdbcBackend

import scala.concurrent.{Await, Future}

class LobbyService(lobbiesManagerActor: ActorRef)(
  implicit db: JdbcBackend.Database,
  system: ActorSystem,
  NKMDataService: NKMDataService,
  gameService: GameService,
) extends NKMTimeouts {
  import com.tosware.NKM.models.CommandResponse._

  def getLobbyActorOption(lobbyId: String): Option[ActorRef] = {
    import LobbiesManager.Query.GetLobbyActor
    import LobbiesManager.GetLobbyActorResponse

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


  def joinLobby(userId: String, request: LobbyJoinRequest): CommandResponse = {
    if(!isLobbyCreated(request.lobbyId)) return Failure("Lobby is not created at this id")

    val lobbyActor = getLobbyActor(request.lobbyId)
    val gameState = aw(getGameState(request.lobbyId))

    if (gameState.gameStatus != GameStatus.NotStarted) return Failure("Game is already started")

    aw(lobbyActor ? Lobby.UserJoin(userId)).asInstanceOf[CommandResponse]
  }

  def leaveLobby(userId: String, request: LobbyLeaveRequest): CommandResponse = {
    if(!isLobbyCreated(request.lobbyId)) return Failure("Lobby is not created at this id")

    val lobbyActor = getLobbyActor(request.lobbyId)
    val gameState = aw(getGameState(request.lobbyId))

    if (gameState.gameStatus != GameStatus.NotStarted) return Failure("Game is already started")

    Await.result(lobbyActor ? Lobby.UserLeave(userId), atMost).asInstanceOf[CommandResponse]
  }

  def setHexmapName(username: String, request: SetHexMapNameRequest): CommandResponse = {
    if(!isLobbyCreated(request.lobbyId)) return Failure("Lobby is not created at this id")

    val lobbyActor = getLobbyActor(request.lobbyId)
    val lobbyState = aw(getLobbyState(lobbyActor))
    val gameState = aw(getGameState(request.lobbyId))
    val hexMaps = NKMDataService.getHexMaps

    if (gameState.gameStatus != GameStatus.NotStarted) return Failure("Game is already started")

    if (!lobbyState.hostUserId.contains(username)) return Failure("You are not the host")

    if (!hexMaps.map(_.name).contains(request.hexMapName)) return Failure("Hexmap with this name does not exist")

    aw(lobbyActor ? Lobby.SetMapName(request.hexMapName)).asInstanceOf[CommandResponse]
  }

  def setNumberOfCharactersPerPlayer(username: String, request: SetNumberOfCharactersPerPlayerRequest): CommandResponse = {
    if(!isLobbyCreated(request.lobbyId)) return Failure("Lobby is not created at this id")

    val lobbyActor = getLobbyActor(request.lobbyId)
    val lobbyState = aw(getLobbyState(lobbyActor))
    val gameState = aw(getGameState(request.lobbyId))

    if (request.charactersPerPlayer < 1) return Failure("Number of characters has to be more than 0")

    if (gameState.gameStatus != GameStatus.NotStarted) return Failure("Game is already started")
    if (!lobbyState.hostUserId.contains(username)) return Failure("You are not the host")

    aw(lobbyActor ? Lobby.SetNumberOfCharactersPerPlayer(request.charactersPerPlayer)).asInstanceOf[CommandResponse]
  }

  def setNumberOfBans(username: String, request: SetNumberOfBansRequest): CommandResponse = {
    if(!isLobbyCreated(request.lobbyId)) return Failure("Lobby is not created at this id")

    val lobbyActor = getLobbyActor(request.lobbyId)
    val lobbyState = aw(getLobbyState(lobbyActor))
    val gameState = aw(getGameState(request.lobbyId))

    if (request.numberOfBans < 0) return Failure("Number of bans has to be more or equal 0")

    if (gameState.gameStatus != GameStatus.NotStarted) return Failure("Game is already started")

    if (!lobbyState.hostUserId.contains(username)) return Failure("You are not the host")

    aw(lobbyActor ? Lobby.SetNumberOfBans(request.numberOfBans)).asInstanceOf[CommandResponse]
  }

  def setPickType(username: String, request: SetPickTypeRequest): CommandResponse = {
    if(!isLobbyCreated(request.lobbyId)) return Failure("Lobby is not created at this id")

    val lobbyActor = getLobbyActor(request.lobbyId)
    val lobbyState = aw(getLobbyState(lobbyActor))
    val gameState = aw(getGameState(request.lobbyId))

    if (gameState.gameStatus != GameStatus.NotStarted) return Failure("Game is already started")

    if (!lobbyState.hostUserId.contains(username)) return Failure("You are not the host")

    aw(lobbyActor ? Lobby.SetPickType(request.pickType)).asInstanceOf[CommandResponse]
  }

  def setLobbyName(username: String, request: SetLobbyNameRequest): CommandResponse = {
    if(!isLobbyCreated(request.lobbyId)) return Failure("Lobby is not created at this id")

    val lobbyActor = getLobbyActor(request.lobbyId)
    val lobbyState = aw(getLobbyState(lobbyActor))
    val gameState = aw(getGameState(request.lobbyId))

    if (gameState.gameStatus != GameStatus.NotStarted) return Failure("Game is already started")

    if (!lobbyState.hostUserId.contains(username)) return Failure("You are not the host")

    aw(lobbyActor ? Lobby.SetLobbyName(request.newName)).asInstanceOf[CommandResponse]
  }

  def setClockConfig(username: String, request: SetClockConfigRequest): CommandResponse = {
    if(!isLobbyCreated(request.lobbyId)) return Failure("Lobby is not created at this id")

    val lobbyActor = getLobbyActor(request.lobbyId)
    val lobbyState = aw(getLobbyState(lobbyActor))
    val gameState = aw(getGameState(request.lobbyId))

    if (gameState.gameStatus != GameStatus.NotStarted) return Failure("Game is already started")

    if (!lobbyState.hostUserId.contains(username)) return Failure("You are not the host")

    aw(lobbyActor ? Lobby.SetClockConfig(request.newConfig)).asInstanceOf[CommandResponse]
  }


  def startGame(username: String, request: StartGameRequest): CommandResponse = {
    if(!isLobbyCreated(request.lobbyId)) return Failure("Lobby is not created at this id")

    val lobbyActor = getLobbyActor(request.lobbyId)
    val lobbyState = aw(getLobbyState(lobbyActor))
    val gameState = aw(getGameState(request.lobbyId))

    if (!lobbyState.hostUserId.contains(username)) return Failure("You are not the host")
    if (lobbyState.chosenHexMapName.isEmpty) return Failure("Chosen hex map name is empty")
    if (lobbyState.userIds.length < 2) return Failure("There are less than 2 players")

    val chosenHexMap: HexMap = NKMDataService.getHexMaps.find(_.name == lobbyState.chosenHexMapName.get).get
    if(chosenHexMap.maxNumberOfCharacters < lobbyState.userIds.length) return Failure("There are more players than allowed for this map")

    if (gameState.gameStatus != GameStatus.NotStarted) return Failure("Game is already started")

    aw(lobbyActor ? Lobby.StartGame(gameService.getGameActor(request.lobbyId))).asInstanceOf[CommandResponse]
  }

  def getAllLobbies(): Future[Seq[LobbyState]] = {
    val readJournal = PersistenceQuery(system).readJournalFor[JdbcReadJournal](JdbcReadJournal.Identifier)
    val lobbyIdsFuture = readJournal.
      currentEventsByTag("lobby", 0)
      .map(_.event.asInstanceOf[Lobby.CreateSuccess])
      .map(_.id)
      .runWith(Sink.seq[String])

    val lobbyIds = Await.result(lobbyIdsFuture, atMost)

    val lobbyActors = lobbyIds.map(lobbyId => system.actorOf(Lobby.props(lobbyId)))
    implicit val ec: scala.concurrent.ExecutionContext = system.dispatcher
    Future.sequence(lobbyActors.map(a => (a ? Lobby.GetState).map(_.asInstanceOf[LobbyState])))
  }
}
