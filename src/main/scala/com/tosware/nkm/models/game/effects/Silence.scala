package com.tosware.nkm.models.game.effects

import com.tosware.nkm.models.game.CharacterEffect.CharacterEffectId
import com.tosware.nkm.models.game.{CharacterEffect, CharacterEffectMetadata, CharacterEffectName, CharacterEffectType}

object Silence {
  val metadata: CharacterEffectMetadata = {
    CharacterEffectMetadata(
      name = CharacterEffectName.Silence,
      initialEffectType = CharacterEffectType.Negative,
      description = "This character cannot use abilities.",
      isCc = true,
    )
  }
}

case class Silence(effectId: CharacterEffectId, initialCooldown: Int) extends CharacterEffect(effectId) {
  val metadata: CharacterEffectMetadata = Silence.metadata
}
