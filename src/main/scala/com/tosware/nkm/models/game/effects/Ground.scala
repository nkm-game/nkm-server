package com.tosware.nkm.models.game.effects

import com.tosware.nkm.models.game.character_effect.{CharacterEffect, CharacterEffectMetadata, CharacterEffectName, CharacterEffectType}
import com.tosware.nkm.models.game.character_effect.CharacterEffect.CharacterEffectId

object Ground {
  val metadata: CharacterEffectMetadata =
    CharacterEffectMetadata(
      name = CharacterEffectName.Ground,
      initialEffectType = CharacterEffectType.Negative,
      description = "This character is grounded and cannot move.",
      isCc = true,
    )
}

case class Ground(effectId: CharacterEffectId, initialCooldown: Int) extends CharacterEffect(effectId) {
  val metadata: CharacterEffectMetadata = Ground.metadata
}
