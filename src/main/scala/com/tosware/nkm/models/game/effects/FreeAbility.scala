package com.tosware.nkm.models.game.effects

import com.tosware.nkm.models.game.CharacterEffect.CharacterEffectId
import com.tosware.nkm.models.game.{CharacterEffect, CharacterEffectMetadata, CharacterEffectName, CharacterEffectType}

object FreeAbility {
  val metadata: CharacterEffectMetadata =
  CharacterEffectMetadata(
    name = CharacterEffectName.FreeAbility,
    initialEffectType = CharacterEffectType.Positive,
    description = "This character can use one of his abilities for free.",
  )
}

case class FreeAbility(effectId: CharacterEffectId, initialCooldown: Int) extends CharacterEffect(effectId) {
  val metadata: CharacterEffectMetadata = FreeAbility.metadata
}
