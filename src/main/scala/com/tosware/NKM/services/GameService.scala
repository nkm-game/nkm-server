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

import scala.concurrent.Future

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


  def placeCharacters(username: String, request: Action.PlaceCharacters): Future[CommandResponse] = {
    val gameActor: ActorRef = getGameActor(request.lobbyId)

    val f = gameActor ? Game.PlaceCharacters(username, request.coordinatesToCharacterIdMap)
    Future.successful(aw(f).asInstanceOf[CommandResponse])
  }

  def moveCharacter(username: String, request: Action.Move): Future[CommandResponse] = {
    val gameActor: ActorRef = getGameActor(request.lobbyId)

    val f = gameActor ? Game.MoveCharacter(username, request.path, request.characterId)
    Future.successful(aw(f).asInstanceOf[CommandResponse])
  }

  def getGameState(gameActor: ActorRef): Future[GameState] =
    (gameActor ? GetState).mapTo[GameState]

  def getGameState(lobbyId: String): Future[GameState] =
    getGameState(getGameActor(lobbyId))

  def getGameStateView(gameActor: ActorRef, forPlayer: Option[PlayerId]): Future[GameStateView] =
    (gameActor ? GetStateView(forPlayer)).mapTo[GameStateView]

  def getGameStateView(lobbyId: String, forPlayer: Option[PlayerId]): Future[GameStateView] =
    getGameStateView(getGameActor(lobbyId), forPlayer)
}
