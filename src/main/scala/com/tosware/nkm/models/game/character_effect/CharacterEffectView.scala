package com.tosware.nkm.models.game.character_effect

import com.tosware.nkm.models.game.character.NkmCharacter.CharacterId
import com.tosware.nkm.models.game.character_effect.CharacterEffect.{CharacterEffectId, CharacterEffectMetadataId}

case class CharacterEffectView
(
  id: CharacterEffectId,
  metadataId: CharacterEffectMetadataId,
  parentCharacterId: CharacterId,
  state: CharacterEffectState,
  effectType: CharacterEffectType,
)
