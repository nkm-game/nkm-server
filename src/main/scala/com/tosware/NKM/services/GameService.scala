package com.tosware.NKM.services

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import com.tosware.NKM.NKMTimeouts
import com.tosware.NKM.actors.Game.GetState
import com.tosware.NKM.actors._
import com.tosware.NKM.models.CommandResponse
import com.tosware.NKM.models.game._
import slick.jdbc.JdbcBackend

import scala.concurrent.{Await, Future}

class GameService(implicit db: JdbcBackend.Database, system: ActorSystem, NKMDataService: NKMDataService) extends NKMTimeouts {

  // TODO: check for placing on spawn or if character is owned by user
  def placeCharacter(userId: String, request: PlaceCharacterRequest): Future[CommandResponse] = {
    val gameActor: ActorRef = system.actorOf(Game.props(request.gameId))
    val gameStateFuture = getGameState(request.gameId)
    val gameState = Await.result(gameStateFuture, atMost)

    if(gameState.gamePhase != GamePhase.CharacterPlacing) return Future.successful(CommandResponse.Failure)
    if(gameState.getCurrentPlayer.name != userId) return Future.successful(CommandResponse.Failure)

    val isCharacterInCharactersOutsideMap = gameState.characterIdsOutsideMap.contains(request.characterId)
    if(!isCharacterInCharactersOutsideMap) return Future.successful(CommandResponse.Failure)

    val isCharacterOfCurrentPlayer = gameState.getCurrentPlayer.characters.map(_.id).contains(request.characterId)
    if(!isCharacterOfCurrentPlayer) return Future.successful(CommandResponse.Failure)

    val targetCellOption = gameState.hexMap.get.getCell(request.hexCoordinates)
    if(targetCellOption.isEmpty) return Future.successful(CommandResponse.Failure)

    val targetCell = targetCellOption.get
    val isTargetCellEmpty = targetCell.characterId.isEmpty
    if(!isTargetCellEmpty) return Future.successful(CommandResponse.Failure)

    if(!gameState.hexMap.get.getSpawnPointsByNumber(gameState.getCurrentPlayerNumber).contains(targetCell))
      return Future.successful(CommandResponse.Failure)

    val placeCharacterFuture = gameActor ? Game.PlaceCharacter(request.hexCoordinates, request.characterId)
    placeCharacterFuture.mapTo[CommandResponse]
  }

  def getGameState(gameId: String): Future[GameState] = {
    val gameActor: ActorRef = system.actorOf(Game.props(gameId))
    (gameActor ? GetState).mapTo[GameState]
  }
}
