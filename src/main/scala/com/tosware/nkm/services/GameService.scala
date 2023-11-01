package com.tosware.nkm.services

import akka.actor.ActorRef
import akka.pattern.ask
import com.tosware.nkm.actors.*
import com.tosware.nkm.actors.Game.*
import com.tosware.nkm.actors.GameIdTrackerActor.Response
import com.tosware.nkm.models.CommandResponse
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.ws.GameRequest.*
import com.tosware.nkm.{NkmTimeouts, *}

import scala.concurrent.Future

class GameService(gameIdTrackerActor: ActorRef)
    extends NkmTimeouts {

  import CommandResponse.*

  private def failGameIdDoesNotExist: Future[Failure] =
    Future.successful(CommandResponse.Failure("Game ID does not exist."))

  def getGameActorOpt(lobbyId: String): Option[ActorRef] =
    aw(gameIdTrackerActor ? GameIdTrackerActor.Query.GetGameActor(lobbyId))
      .asInstanceOf[GameIdTrackerActor.Response] match {
      case Response.GetGameActorResponse(gameActor) => Some(gameActor)
      case Response.GameIdDoesNotExist              => None
      case _                                        => None
    }

  def pause(username: String, lobbyId: String): Future[CommandResponse] = {
    val gameActor: ActorRef = getGameActorOpt(lobbyId).getOrElse(return failGameIdDoesNotExist)

    (gameActor ? Game.Pause(username)).mapTo[CommandResponse]
  }

  def surrender(username: String, lobbyId: String): Future[CommandResponse] = {
    val gameActor: ActorRef = getGameActorOpt(lobbyId).getOrElse(return failGameIdDoesNotExist)

    (gameActor ? Game.Surrender(username)).mapTo[CommandResponse]
  }

  def banCharacters(username: String, request: CharacterSelect.BanCharacters): Future[CommandResponse] = {
    val gameActor: ActorRef = getGameActorOpt(request.lobbyId).getOrElse(return failGameIdDoesNotExist)

    (gameActor ? Game.BanCharacters(username, request.characterIds)).mapTo[CommandResponse]
  }

  def pickCharacter(username: String, request: CharacterSelect.PickCharacter): Future[CommandResponse] = {
    val gameActor: ActorRef = getGameActorOpt(request.lobbyId).getOrElse(return failGameIdDoesNotExist)

    (gameActor ? Game.PickCharacter(username, request.characterId)).mapTo[CommandResponse]
  }

  def blindPickCharacter(username: String, request: CharacterSelect.BlindPickCharacters): Future[CommandResponse] = {
    val gameActor: ActorRef = getGameActorOpt(request.lobbyId).getOrElse(return failGameIdDoesNotExist)

    (gameActor ? Game.BlindPickCharacters(username, request.characterIds)).mapTo[CommandResponse]
  }

  def placeCharacters(username: String, request: Action.PlaceCharacters): Future[CommandResponse] = {
    val gameActor: ActorRef = getGameActorOpt(request.lobbyId).getOrElse(return failGameIdDoesNotExist)

    (gameActor ? Game.PlaceCharacters(username, request.coordinatesToCharacterIdMap)).mapTo[CommandResponse]
  }

  def endTurn(username: String, request: Action.EndTurn): Future[CommandResponse] = {
    val gameActor: ActorRef = getGameActorOpt(request.lobbyId).getOrElse(return failGameIdDoesNotExist)

    (gameActor ? Game.EndTurn(username)).mapTo[CommandResponse]
  }

  def passTurn(username: String, request: Action.PassTurn): Future[CommandResponse] = {
    val gameActor: ActorRef = getGameActorOpt(request.lobbyId).getOrElse(return failGameIdDoesNotExist)

    (gameActor ? Game.PassTurn(username, request.characterId)).mapTo[CommandResponse]
  }

  def moveCharacter(username: String, request: Action.Move): Future[CommandResponse] = {
    val gameActor: ActorRef = getGameActorOpt(request.lobbyId).getOrElse(return failGameIdDoesNotExist)

    (gameActor ? Game.MoveCharacter(username, request.path, request.characterId)).mapTo[CommandResponse]
  }

  def basicAttackCharacter(username: String, request: Action.BasicAttack): Future[CommandResponse] = {
    val gameActor: ActorRef = getGameActorOpt(request.lobbyId).getOrElse(return failGameIdDoesNotExist)

    (gameActor ? Game.BasicAttackCharacter(username, request.attackingCharacterId, request.targetCharacterId))
      .mapTo[CommandResponse]
  }

  def useAbility(username: String, request: Action.UseAbility): Future[CommandResponse] = {
    val gameActor: ActorRef = getGameActorOpt(request.lobbyId).getOrElse(return failGameIdDoesNotExist)

    (gameActor ? Game.UseAbility(username, request.abilityId, request.useData)).mapTo[CommandResponse]
  }

  def useAbilityOnCoordinates(username: String, request: Action.UseAbilityOnCoordinates): Future[CommandResponse] = {
    val gameActor: ActorRef = getGameActorOpt(request.lobbyId).getOrElse(return failGameIdDoesNotExist)

    (gameActor ? Game.UseAbilityOnCoordinates(username, request.abilityId, request.target, request.useData))
      .mapTo[CommandResponse]
  }

  def useAbilityOnCharacter(username: String, request: Action.UseAbilityOnCharacter): Future[CommandResponse] = {
    val gameActor: ActorRef = getGameActorOpt(request.lobbyId).getOrElse(return failGameIdDoesNotExist)

    (gameActor ? Game.UseAbilityOnCharacter(username, request.abilityId, request.target, request.useData))
      .mapTo[CommandResponse]
  }

  def getGameState(gameActor: ActorRef): Future[GameState] =
    (gameActor ? GetState).mapTo[GameState]

  def getGameState(lobbyId: String): Option[Future[GameState]] =
    getGameActorOpt(lobbyId).map(getGameState)

  def getGameStateView(gameActor: ActorRef, forPlayer: Option[PlayerId]): Future[GameStateView] =
    (gameActor ? GetStateView(forPlayer)).mapTo[GameStateView]

  def getGameStateViewOpt(lobbyId: String, forPlayer: Option[PlayerId]): Option[Future[GameStateView]] =
    getGameActorOpt(lobbyId).map(gameActor => getGameStateView(gameActor, forPlayer))

  def getCurrentClock(gameActor: ActorRef): Future[Clock] =
    (gameActor ? GetCurrentClock).mapTo[Clock]

  def getCurrentClockOpt(lobbyId: String): Option[Future[Clock]] =
    getGameActorOpt(lobbyId).map(getCurrentClock)
}
