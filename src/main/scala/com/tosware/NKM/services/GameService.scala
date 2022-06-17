package com.tosware.NKM.services

import akka.actor.ActorRef
import akka.pattern.ask
import com.tosware.NKM.NKMTimeouts
import com.tosware.NKM.actors.Game.{GetState, GetStateView}
import com.tosware.NKM.actors._
import com.tosware.NKM.models.CommandResponse
import com.tosware.NKM.models.game.Player.PlayerId
import com.tosware.NKM.models.game._
import com.tosware.NKM.models.game.ws.GameRequest._
import slick.jdbc.JdbcBackend

import scala.concurrent.{Await, Future}

class GameService(gamesManagerActor: ActorRef)
                 (implicit db: JdbcBackend.Database, NKMDataService: NKMDataService)
  extends NKMTimeouts {
  import CommandResponse._

  def getGameActor(lobbyId: String): ActorRef = {
    import GamesManager.GetGameActorResponse
    import GamesManager.Query.GetGameActor

    aw(gamesManagerActor ? GetGameActor(lobbyId))
      .asInstanceOf[GetGameActorResponse]
      .gameActor
  }

  def pause(username: String, lobbyId: String) = {
    val gameActor: ActorRef = getGameActor(lobbyId)

    val pauseFuture = gameActor ? Game.Pause(username)
    Future.successful(aw(pauseFuture).asInstanceOf[CommandResponse])
  }

  def surrender(username: String, lobbyId: String): Future[CommandResponse] = {
    val gameActor: ActorRef = getGameActor(lobbyId)

    val surrenderFuture = gameActor ? Game.Surrender(username)
    Future.successful(aw(surrenderFuture).asInstanceOf[CommandResponse])
  }

  def banCharacters(username: String, request: CharacterSelect.BanCharacters): Future[CommandResponse] = {
    val gameActor: ActorRef = getGameActor(request.lobbyId)

    val requestFuture = gameActor ? Game.BanCharacters(username, request.characterIds)
    Future.successful(aw(requestFuture).asInstanceOf[CommandResponse])
  }

  def pickCharacter(username: String, request: CharacterSelect.PickCharacter): Future[CommandResponse] = {
    val gameActor: ActorRef = getGameActor(request.lobbyId)

    val f = gameActor ? Game.PickCharacter(username, request.characterId)
    Future.successful(aw(f).asInstanceOf[CommandResponse])
  }

  def blindPickCharacter(username: String, request: CharacterSelect.BlindPickCharacters): Future[CommandResponse] = {
    val gameActor: ActorRef = getGameActor(request.lobbyId)

    val f = gameActor ? Game.BlindPickCharacters(username, request.characterIds)
    Future.successful(aw(f).asInstanceOf[CommandResponse])
  }

  // TODO: check for placing on spawn or if character is owned by user
//  def placeCharacters(userId: String, request: PlaceCharactersRequest): Future[CommandResponse] = {
//    val gameActor: ActorRef = system.actorOf(Game.props(request.lobbyId))
//    val gameStateFuture = getGameState(request.lobbyId)
//    val gameState = Await.result(gameStateFuture, atMost)
//
//    if(gameState.gamePhase != GamePhase.CharacterPlacing) return Future.successful(CommandResponse.Failure)
//    if(gameState.getCurrentPlayer.name != userId) return Future.successful(CommandResponse.Failure)
//
//    val isCharacterInCharactersOutsideMap = gameState.characterIdsOutsideMap.contains(request.characterId)
//    if(!isCharacterInCharactersOutsideMap) return Future.successful(CommandResponse.Failure)
//
//    val isCharacterOfCurrentPlayer = gameState.getCurrentPlayer.characters.map(_.id).contains(request.characterId)
//    if(!isCharacterOfCurrentPlayer) return Future.successful(CommandResponse.Failure)
//
//    val targetCellOption = gameState.hexMap.get.getCell(request.hexCoordinates)
//    if(targetCellOption.isEmpty) return Future.successful(CommandResponse.Failure)
//
//    val targetCell = targetCellOption.get
//    val isTargetCellEmpty = targetCell.characterId.isEmpty
//    if(!isTargetCellEmpty) return Future.successful(CommandResponse.Failure)
//
//    if(!gameState.hexMap.get.getSpawnPointsByNumber(gameState.getCurrentPlayerNumber).contains(targetCell))
//      return Future.successful(CommandResponse.Failure)
//
//    val placeCharacterFuture = gameActor ? Game.PlaceCharacter(request.hexCoordinates, request.characterId)
//    placeCharacterFuture.mapTo[CommandResponse]
//  }

  def getGameState(gameActor: ActorRef): Future[GameState] =
    (gameActor ? GetState).mapTo[GameState]

  def getGameState(lobbyId: String): Future[GameState] =
    getGameState(getGameActor(lobbyId))

  def getGameStateView(gameActor: ActorRef, forPlayer: Option[PlayerId]): Future[GameStateView] =
    (gameActor ? GetStateView(forPlayer)).mapTo[GameStateView]

  def getGameStateView(lobbyId: String, forPlayer: Option[PlayerId]): Future[GameStateView] =
    getGameStateView(getGameActor(lobbyId), forPlayer)
}
