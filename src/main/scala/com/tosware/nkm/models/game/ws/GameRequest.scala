package com.tosware.nkm.models.game.ws

import com.tosware.nkm.*
import com.tosware.nkm.models.game.ability.UseData
import com.tosware.nkm.models.game.hex.HexCoordinates

object GameRequest {
  sealed trait GameRequest
  object General {
    final case class Auth(token: String) extends GameRequest
    final case class Observe(lobbyId: String) extends GameRequest
    final case class GetState(lobbyId: String) extends GameRequest
    final case class GetCurrentClock(lobbyId: String) extends GameRequest
    final case class Pause(lobbyId: String) extends GameRequest
    final case class Surrender(lobbyId: String) extends GameRequest
  }
  object CharacterSelect {
    final case class BanCharacters(lobbyId: GameId, characterIds: Set[CharacterMetadataId]) extends GameRequest
    final case class PickCharacter(lobbyId: GameId, characterId: CharacterMetadataId) extends GameRequest
    final case class BlindPickCharacters(lobbyId: GameId, characterIds: Set[CharacterMetadataId]) extends GameRequest
  }
  object Action {
    final case class PlaceCharacters(lobbyId: GameId, coordinatesToCharacterIdMap: Map[HexCoordinates, CharacterId]) extends GameRequest
    final case class EndTurn(lobbyId: GameId) extends GameRequest
    final case class PassTurn(lobbyId: GameId, characterId: CharacterId) extends GameRequest
    final case class Move(lobbyId: GameId, path: Seq[HexCoordinates], characterId: CharacterId) extends GameRequest
    final case class BasicAttack(lobbyId: GameId, attackingCharacterId: CharacterId, targetCharacterId: CharacterId) extends GameRequest
    final case class UseAbility(lobbyId: GameId, abilityId: AbilityId, useData: UseData) extends GameRequest
    final case class UseAbilityOnCoordinates(lobbyId: GameId, abilityId: AbilityId, target: HexCoordinates, useData: UseData) extends GameRequest
    final case class UseAbilityOnCharacter(lobbyId: GameId, abilityId: AbilityId, target: CharacterId, useData: UseData) extends GameRequest
  }
  object Chat {
    final case class SendChatMessage(lobbyId: GameId, message: String) extends GameRequest
    final case class ExecuteCommand(lobbyId: GameId, command: String) extends GameRequest
  }
}