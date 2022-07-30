package com.tosware.NKM.models.game.ws

import com.tosware.NKM.models.game.NKMCharacter.CharacterId
import com.tosware.NKM.models.game.NKMCharacterMetadata.CharacterMetadataId
import com.tosware.NKM.models.game.hex.HexCoordinates

object GameRequest {
  sealed trait GameRequest
  object General {
    final case class Auth(token: String) extends GameRequest
    final case class Observe(lobbyId: String) extends GameRequest
    final case class GetState(lobbyId: String) extends GameRequest
    final case class Pause(lobbyId: String) extends GameRequest
    final case class Surrender(lobbyId: String) extends GameRequest
  }
  object CharacterSelect {
    final case class BanCharacters(lobbyId: String, characterIds: Set[CharacterMetadataId]) extends GameRequest
    final case class PickCharacter(lobbyId: String, characterId: CharacterMetadataId) extends GameRequest
    final case class BlindPickCharacters(lobbyId: String, characterIds: Set[CharacterMetadataId]) extends GameRequest
  }
  object Action {
    final case class PlaceCharacters(lobbyId: String, coordinatesToCharacterIdMap: Map[HexCoordinates, CharacterId]) extends GameRequest
    final case class EndTurn(lobbyId: String) extends GameRequest
    final case class Move(lobbyId: String, path: Seq[HexCoordinates], characterId: CharacterId) extends GameRequest
    final case class BasicAttack(lobbyId: String, characterThatAttacksId: CharacterId, targetCharacterId: CharacterId) extends GameRequest
    final case class UseAbility(lobbyId: String, useAbilityData: String) extends GameRequest
  }
  object Chat {
    final case class SendChatMessage(lobbyId: String, message: String) extends GameRequest
    final case class ExecuteCommand(lobbyId: String, command: String) extends GameRequest
  }
}