package com.tosware.NKM.models.game.effects

import com.tosware.NKM.models.game.{CharacterEffect, CharacterEffectMetadata, CharacterEffectName, CharacterEffectType}

object SnareEffect {
  val metadata: CharacterEffectMetadata =
    CharacterEffectMetadata(
      name = CharacterEffectName.Snare,
      effectType = CharacterEffectType.Negative,
      description = "This character cannot basic move.",
      isCc = true,
    )
}

case class SnareEffect(cooldown: Int) extends CharacterEffect {
  val metadata: CharacterEffectMetadata = SnareEffect.metadata
}
