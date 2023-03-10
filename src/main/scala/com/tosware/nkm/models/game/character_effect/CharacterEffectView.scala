package com.tosware.nkm.models.game.character_effect

import com.tosware.nkm._

case class CharacterEffectView
(
  id: CharacterEffectId,
  metadataId: CharacterEffectMetadataId,
  parentCharacterId: CharacterId,
  state: CharacterEffectState,
  effectType: CharacterEffectType,
)
