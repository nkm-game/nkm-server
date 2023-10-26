package com.tosware.nkm.models.game.effects

import com.tosware.nkm.*
import com.tosware.nkm.models.game.character_effect.*

object FreeAbility {
  val metadata: CharacterEffectMetadata =
    CharacterEffectMetadata(
      name = CharacterEffectName.FreeAbility,
      initialEffectType = CharacterEffectType.Positive,
      description = "You can use one of your abilities for free.",
    )
}

case class FreeAbility(effectId: CharacterEffectId, initialCooldown: Int) extends CharacterEffect(effectId) {
  val metadata: CharacterEffectMetadata = FreeAbility.metadata
}
