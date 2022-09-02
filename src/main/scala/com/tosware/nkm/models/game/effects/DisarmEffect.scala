package com.tosware.nkm.models.game.effects

import com.tosware.nkm.models.game.CharacterEffect.CharacterEffectId
import com.tosware.nkm.models.game.{CharacterEffect, CharacterEffectMetadata, CharacterEffectName, CharacterEffectType}

object DisarmEffect {
  val metadata: CharacterEffectMetadata =
    CharacterEffectMetadata(
      name = CharacterEffectName.Disarm,
      effectType = CharacterEffectType.Negative,
      description = "This character is disarmed and cannot use basic attacks.",
      isCc = true,
    )
}

case class DisarmEffect(effectId: CharacterEffectId, cooldown: Int) extends CharacterEffect(effectId) {
  val metadata: CharacterEffectMetadata = DisarmEffect.metadata
}