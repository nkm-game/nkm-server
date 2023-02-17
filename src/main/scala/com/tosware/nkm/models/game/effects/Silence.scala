package com.tosware.nkm.models.game.effects

import com.tosware.nkm.models.game.character_effect.{CharacterEffect, CharacterEffectMetadata, CharacterEffectName, CharacterEffectType}
import com.tosware.nkm.models.game.character_effect.CharacterEffect.CharacterEffectId

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
