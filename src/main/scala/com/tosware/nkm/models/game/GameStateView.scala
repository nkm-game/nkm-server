package com.tosware.nkm.models.game

import com.tosware.nkm._
import com.tosware.nkm.models.game.ability.AbilityView
import com.tosware.nkm.models.game.character.{CharacterMetadata, NkmCharacterView}
import com.tosware.nkm.models.game.character_effect.CharacterEffectView
import com.tosware.nkm.models.game.event.GameLogView
import com.tosware.nkm.models.game.hex.HexMapView
import com.tosware.nkm.models.game.pick.PickType
import com.tosware.nkm.models.game.pick.blindpick.BlindPickStateView
import com.tosware.nkm.models.game.pick.draftpick.DraftPickStateView

case class GameStateView(
  id: GameId,
  charactersMetadata: Set[CharacterMetadata],
  gameStatus: GameStatus,
  pickType: PickType,
  numberOfBans: Int,
  numberOfCharactersPerPlayers: Int,
  draftPickState: Option[DraftPickStateView],
  blindPickState: Option[BlindPickStateView],
  hexMap: HexMapView,
  players: Seq[Player],
  characters: Set[NkmCharacterView],
  abilities: Set[AbilityView],
  effects: Set[CharacterEffectView],
  phase: Phase,
  turn: Turn,
  characterIdsOutsideMap: Set[CharacterId],
  characterIdsThatTookActionThisPhase: Set[CharacterId],
  characterTakingActionThisTurn: Option[CharacterId],
  playerIdsThatPlacedCharacters: Set[PlayerId],
  clockConfig: ClockConfig,
  clock: Clock,
  gameLog: GameLogView,

  currentPlayerId: PlayerId,
  hostId: PlayerId,
  isBlindPickingPhase: Boolean,
  isDraftBanningPhase: Boolean,
  isInCharacterSelect: Boolean,
  isSharedTime: Boolean,
  currentPlayerTime: Long,
  charactersToTakeAction: Set[CharacterId],
)
