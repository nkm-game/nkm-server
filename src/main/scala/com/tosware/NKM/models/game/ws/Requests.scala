package com.tosware.NKM.models.game.ws

import com.tosware.NKM.models.game.HexCoordinates
import com.tosware.NKM.models.game.NKMCharacterMetadata.CharacterMetadataId

sealed trait GameRequest

// General
final case class AuthRequest(token: String) extends GameRequest
final case class ObserveRequest(gameId: String) extends GameRequest
final case class GetStateRequest(gameId: String) extends GameRequest
final case class PauseRequest(gameId: String) extends GameRequest
final case class SurrenderRequest(gameId: String) extends GameRequest

// Character Select
final case class BanCharactersRequest(gameId: String, characterIds: Set[CharacterMetadataId]) extends GameRequest
final case class PickCharacterRequest(gameId: String, characterId: CharacterMetadataId) extends GameRequest
final case class BlindPickCharactersRequest(gameId: String, characterIds: Set[CharacterMetadataId]) extends GameRequest

// Actions
final case class PlaceCharactersRequest(gameId: String, coordinatesToCharacterIdMap: Map[HexCoordinates, String]) extends GameRequest
final case class EndTurnRequest(gameId: String) extends GameRequest
final case class MoveRequest(gameId: String, path: Seq[HexCoordinates], characterId: String) extends GameRequest
final case class BasicAttackRequest(gameId: String, characterThatAttacksId: String, targetCharacterId: String) extends GameRequest
final case class UseAbilityRequest(gameId: String, useAbilityData: String) extends GameRequest

// Chat
final case class SendChatMessage(gameId: String, message: String) extends GameRequest
final case class ExecuteCommand(gameId: String, command: String) extends GameRequest
