package com.tosware.NKM.models.game.effects

import com.tosware.NKM.models.game.{CharacterEffect, CharacterEffectMetadata, CharacterEffectName, CharacterEffectType}

object GroundEffect {
  val metadata: CharacterEffectMetadata =
    CharacterEffectMetadata(
      name = CharacterEffectName.Ground,
      effectType = CharacterEffectType.Negative,
      description = "This character is grounded and cannot move.",
      isCc = true,
    )
}

case class GroundEffect(cooldown: Int) extends CharacterEffect {
  val metadata: CharacterEffectMetadata = GroundEffect.metadata
}
