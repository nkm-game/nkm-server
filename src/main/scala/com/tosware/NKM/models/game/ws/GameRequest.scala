package com.tosware.NKM.models.game.ws

import com.tosware.NKM.models.game.HexCoordinates
import com.tosware.NKM.models.game.NKMCharacterMetadata.CharacterMetadataId

object GameRequest {
  sealed trait GameRequest
  object General {
    final case class Auth(token: String) extends GameRequest
    final case class Observe(gameId: String) extends GameRequest
    final case class GetState(gameId: String) extends GameRequest
    final case class Pause(gameId: String) extends GameRequest
    final case class Surrender(gameId: String) extends GameRequest
  }
  object CharacterSelect {
    final case class BanCharacters(gameId: String, characterIds: Set[CharacterMetadataId]) extends GameRequest
    final case class PickCharacter(gameId: String, characterId: CharacterMetadataId) extends GameRequest
    final case class BlindPickCharacters(gameId: String, characterIds: Set[CharacterMetadataId]) extends GameRequest
  }
  object Action {
    final case class PlaceCharacters(gameId: String, coordinatesToCharacterIdMap: Map[HexCoordinates, String]) extends GameRequest
    final case class EndTurn(gameId: String) extends GameRequest
    final case class Move(gameId: String, path: Seq[HexCoordinates], characterId: String) extends GameRequest
    final case class BasicAttack(gameId: String, characterThatAttacksId: String, targetCharacterId: String) extends GameRequest
    final case class UseAbility(gameId: String, useAbilityData: String) extends GameRequest
  }
  object Chat {
    final case class SendChatMessage(gameId: String, message: String) extends GameRequest
    final case class ExecuteCommand(gameId: String, command: String) extends GameRequest
  }
}