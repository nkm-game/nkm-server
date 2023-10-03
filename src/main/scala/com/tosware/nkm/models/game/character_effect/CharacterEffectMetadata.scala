package com.tosware.nkm.models.game.character_effect

import com.tosware.nkm.*

case class CharacterEffectMetadata(
    name: CharacterEffectName,
    initialEffectType: CharacterEffectType,
    description: String,
    isCc: Boolean = false,
) {
  def id: CharacterEffectMetadataId = name.entryName
}
