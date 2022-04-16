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

object GameService {
  sealed trait Event
  case class Success(msg: String = "") extends Event
  case class Failure(msg: String = "") extends Event
}

class GameService(implicit db: JdbcBackend.Database, system: ActorSystem, NKMDataService: NKMDataService) extends NKMTimeouts {
  import GameService._

  def surrender(username: String, gameId: String): Future[Event] = {
    implicit val gameActor: ActorRef = system.actorOf(Game.props(gameId))
    val gameStateFuture = getGameState()
    val gameState: GameState = Await.result(gameStateFuture, atMost)

    val playerOption = gameState.players.find(_.name == username)
    if(playerOption.isEmpty) return Future.successful(Failure("This player is not in this game."))
    val player = playerOption.get
    if(player.victoryStatus != VictoryStatus.Pending) return Future.successful(Failure("This player already finished the game."))

    val surrenderFuture = gameActor ? Game.Surrender(username)
    Await.result(surrenderFuture, atMost) match {
      case CommandResponse.Success => Future.successful(Success())
      case CommandResponse.Failure => Future.successful(Failure())
    }
  }

  // TODO: check for placing on spawn or if character is owned by user
//  def placeCharacters(userId: String, request: PlaceCharactersRequest): Future[CommandResponse] = {
//    val gameActor: ActorRef = system.actorOf(Game.props(request.gameId))
//    val gameStateFuture = getGameState(request.gameId)
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
  def getGameState(gameId: String): Future[GameState] = {
    implicit val gameActor: ActorRef = system.actorOf(Game.props(gameId))
    getGameState()
  }
  def getGameState()(implicit gameActor: ActorRef): Future[GameState] = {
    (gameActor ? GetState).mapTo[GameState]
  }
}
