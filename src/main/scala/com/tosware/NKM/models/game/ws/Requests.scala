package com.tosware.NKM.models.game.ws

import com.tosware.NKM.models.game.HexCoordinates

sealed trait GameRequest

// General
final case class AuthRequest(token: String) extends GameRequest
final case class ObserveRequest(gameId: String) extends GameRequest
final case class GetStateRequest(gameId: String) extends GameRequest
final case class PauseRequest(gameId: String) extends GameRequest
final case class SurrenderRequest(gameId: String) extends GameRequest

// Character Select
final case class BanCharactersRequest(gameId: String, characterIdsOpt: Option[Seq[String]]) extends GameRequest
final case class PickCharactersRequest(gameId: String, characterIds: Seq[String]) extends GameRequest

// Actions
final case class PlaceCharactersRequest(gameId: String, coordinatesToCharacterIdMap: Map[HexCoordinates, String]) extends GameRequest
final case class EndTurnRequest(gameId: String) extends GameRequest
final case class MoveRequest(gameId: String, path: Seq[HexCoordinates], characterId: String) extends GameRequest
final case class BasicAttackRequest(gameId: String, characterThatAttacksId: String, targetCharacterId: String) extends GameRequest
final case class UseAbilityRequest(gameId: String, useAbilityData: String) extends GameRequest

// Chat
final case class SendChatMessage(gameId: String, message: String) extends GameRequest
final case class ExecuteCommand(gameId: String, command: String) extends GameRequest
