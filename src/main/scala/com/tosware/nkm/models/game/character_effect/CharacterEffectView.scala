package com.tosware.nkm.models.game.character_effect

import com.tosware.nkm.*

case class CharacterEffectView
(
  id: CharacterEffectId,
  metadataId: CharacterEffectMetadataId,
  parentCharacterId: CharacterId,
  state: CharacterEffectState,
  effectType: CharacterEffectType,
)
