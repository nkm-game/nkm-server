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

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}

class LobbyService(lobbiesManagerActor: ActorRef)(implicit db: JdbcBackend.Database,
                   system: ActorSystem,
                   NKMDataService: NKMDataService,
                  ) extends NKMTimeouts {
  import com.tosware.NKM.models.CommandResponse._

  def createLobby(name: String, hostUserId: String): CommandResponse =
    Await.result(lobbiesManagerActor ? Lobby.Create(name, hostUserId), atMost).asInstanceOf[CommandResponse]

  def joinLobby(userId: String, request: LobbyJoinRequest): CommandResponse = {
    val gameActor: ActorRef = system.actorOf(Game.props(request.lobbyId))
    val gameState = Await.result(gameActor ? Game.GetState, atMost).asInstanceOf[GameState]
    if (gameState.gamePhase != GamePhase.NotStarted) return Failure("Game is already started")

    val lobbyActor: ActorRef = system.actorOf(Lobby.props(request.lobbyId))

    Await.result(lobbyActor ? Lobby.UserJoin(userId), atMost).asInstanceOf[CommandResponse]
  }

  def leaveLobby(userId: String, request: LobbyLeaveRequest): CommandResponse = {
    val gameActor: ActorRef = system.actorOf(Game.props(request.lobbyId))
    val gameState = Await.result(gameActor ? Game.GetState, atMost).asInstanceOf[GameState]
    if (gameState.gamePhase != GamePhase.NotStarted) return Failure("Game is already started")

    val lobbyActor: ActorRef = system.actorOf(Lobby.props(request.lobbyId))

    Await.result(lobbyActor ? Lobby.UserLeave(userId), atMost).asInstanceOf[CommandResponse]
  }

  def setHexmapName(username: String, request: SetHexMapNameRequest): CommandResponse = {
    val gameActor: ActorRef = system.actorOf(Game.props(request.lobbyId))
    val gameState = Await.result(gameActor ? Game.GetState, atMost).asInstanceOf[GameState]
    if (gameState.gamePhase != GamePhase.NotStarted) return Failure("Game is already started")

    val lobbyActor: ActorRef = system.actorOf(Lobby.props(request.lobbyId))
    val nkmDataActor: ActorRef = system.actorOf(NKMData.props())

    val lobbyState = Await.result(lobbyActor ? Lobby.GetState, atMost).asInstanceOf[LobbyState]
    if (!lobbyState.hostUserId.contains(username)) return Failure("You are not the host")

    val hexMaps = Await.result(nkmDataActor ? NKMData.GetHexMaps, atMost).asInstanceOf[List[HexMap]]
    if (!hexMaps.map(_.name).contains(request.hexMapName)) return Failure("Hexmap with this name does not exist")

    Await.result(lobbyActor ? Lobby.SetMapName(request.hexMapName), atMost).asInstanceOf[CommandResponse]
  }

  def setNumberOfCharactersPerPlayer(username: String, request: SetNumberOfCharactersPerPlayerRequest): CommandResponse = {
    if (request.charactersPerPlayer < 1) return Failure("Number of characters has to be more than 0")

    val gameActor: ActorRef = system.actorOf(Game.props(request.lobbyId))
    val gameState = Await.result(gameActor ? Game.GetState, atMost).asInstanceOf[GameState]
    if (gameState.gamePhase != GamePhase.NotStarted) return Failure("Game is already started")

    val lobbyActor: ActorRef = system.actorOf(Lobby.props(request.lobbyId))

    val lobbyState = Await.result(lobbyActor ? Lobby.GetState, atMost).asInstanceOf[LobbyState]
    if (!lobbyState.hostUserId.contains(username)) return Failure("You are not the host")

    Await.result(lobbyActor ? Lobby.SetNumberOfCharactersPerPlayer(request.charactersPerPlayer), atMost).asInstanceOf[CommandResponse]
  }

  def setNumberOfBans(username: String, request: SetNumberOfBansRequest): CommandResponse = {
    if (request.numberOfBans < 0) return Failure("Number of bans has to be more or equal 0")

    val gameActor: ActorRef = system.actorOf(Game.props(request.lobbyId))
    val gameState = Await.result(gameActor ? Game.GetState, atMost).asInstanceOf[GameState]
    if (gameState.gamePhase != GamePhase.NotStarted) return Failure("Game is already started")

    val lobbyActor: ActorRef = system.actorOf(Lobby.props(request.lobbyId))

    val lobbyState = Await.result(lobbyActor ? Lobby.GetState, atMost).asInstanceOf[LobbyState]
    if (!lobbyState.hostUserId.contains(username)) return Failure("You are not the host")

    Await.result(lobbyActor ? Lobby.SetNumberOfBans(request.numberOfBans), atMost).asInstanceOf[CommandResponse]
  }

  def setPickType(username: String, request: SetPickTypeRequest): CommandResponse = {
    val gameActor: ActorRef = system.actorOf(Game.props(request.lobbyId))
    val gameState = Await.result(gameActor ? Game.GetState, atMost).asInstanceOf[GameState]
    if (gameState.gamePhase != GamePhase.NotStarted) return Failure("Game is already started")

    val lobbyActor: ActorRef = system.actorOf(Lobby.props(request.lobbyId))

    val lobbyState = Await.result(lobbyActor ? Lobby.GetState, atMost).asInstanceOf[LobbyState]
    if (!lobbyState.hostUserId.contains(username)) return Failure("You are not the host")

    Await.result(lobbyActor ? Lobby.SetPickType(request.pickType), atMost).asInstanceOf[CommandResponse]
  }

  def setLobbyName(username: String, request: SetLobbyNameRequest): CommandResponse = {
    val gameActor: ActorRef = system.actorOf(Game.props(request.lobbyId))
    val gameState = Await.result(gameActor ? Game.GetState, atMost).asInstanceOf[GameState]
    if (gameState.gamePhase != GamePhase.NotStarted) return Failure("Game is already started")

    val lobbyActor: ActorRef = system.actorOf(Lobby.props(request.lobbyId))

    val lobbyState = Await.result(lobbyActor ? Lobby.GetState, atMost).asInstanceOf[LobbyState]
    if (!lobbyState.hostUserId.contains(username)) return Failure("You are not the host")

    Await.result(lobbyActor ? Lobby.SetLobbyName(request.newName), atMost).asInstanceOf[CommandResponse]
  }

  def setClockConfig(username: String, request: SetClockConfigRequest): CommandResponse = {
    val gameActor: ActorRef = system.actorOf(Game.props(request.lobbyId))
    val gameState = Await.result(gameActor ? Game.GetState, atMost).asInstanceOf[GameState]
    if (gameState.gamePhase != GamePhase.NotStarted) return Failure("Game is already started")

    val lobbyActor: ActorRef = system.actorOf(Lobby.props(request.lobbyId))

    val lobbyState = Await.result(lobbyActor ? Lobby.GetState, atMost).asInstanceOf[LobbyState]
    if (!lobbyState.hostUserId.contains(username)) return Failure("You are not the host")

    Await.result(lobbyActor ? Lobby.SetClockConfig(request.newConfig), atMost).asInstanceOf[CommandResponse]
  }


  def startGame(username: String, request: StartGameRequest): CommandResponse = {
    val lobbyActor: ActorRef = system.actorOf(Lobby.props(request.lobbyId))
    val gameActor: ActorRef = system.actorOf(Game.props(request.lobbyId))

    val lobbyState = Await.result(lobbyActor ? Lobby.GetState, atMost).asInstanceOf[LobbyState]

    if (!lobbyState.hostUserId.contains(username)) return Failure("You are not the host")
    if (lobbyState.chosenHexMapName.isEmpty) return Failure("Chosen hex map name is empty")
    if (lobbyState.userIds.length < 2) return Failure("There are less than 2 players")

    val chosenHexMap: HexMap = NKMDataService.getHexMaps.find(_.name == lobbyState.chosenHexMapName.get).get
    if(chosenHexMap.maxNumberOfCharacters < lobbyState.userIds.length) return Failure("There are more players than allowed for this map")

    val gameState = Await.result(gameActor ? Game.GetState, atMost).asInstanceOf[GameState]
    if (gameState.gamePhase != GamePhase.NotStarted) return Failure("Game is already started")

    Await.result(lobbyActor ? Lobby.StartGame, atMost).asInstanceOf[CommandResponse]
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
    Future.sequence(lobbyActors.map(a => (a ? Lobby.GetState).map(_.asInstanceOf[LobbyState])))
  }

  def getLobby(lobbyId: String): Future[LobbyState] = {
    val lobbyActor: ActorRef = system.actorOf(Lobby.props(lobbyId))
    val lobbyStateFuture: Future[LobbyState] = (lobbyActor ? Lobby.GetState).map(x => x.asInstanceOf[LobbyState])
    lobbyStateFuture
  }
}
