package com.tosware.nkm.models.game.effects

import com.tosware.nkm.models.game.CharacterEffect.CharacterEffectId
import com.tosware.nkm.models.game.{CharacterEffect, CharacterEffectMetadata, CharacterEffectName, CharacterEffectType}

object Ground {
  val metadata: CharacterEffectMetadata =
    CharacterEffectMetadata(
      name = CharacterEffectName.Ground,
      initialEffectType = CharacterEffectType.Negative,
      description = "This characterOpt is grounded and cannot move.",
      isCc = true,
    )
}

case class Ground(effectId: CharacterEffectId, initialCooldown: Int) extends CharacterEffect(effectId) {
  val metadata: CharacterEffectMetadata = Ground.metadata
}
