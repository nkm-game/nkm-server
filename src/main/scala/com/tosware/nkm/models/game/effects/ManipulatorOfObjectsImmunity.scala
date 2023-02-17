package com.tosware.nkm.models.game.effects

import com.tosware.nkm.models.game.character_effect.{CharacterEffect, CharacterEffectMetadata, CharacterEffectName, CharacterEffectType}
import com.tosware.nkm.models.game.character_effect.CharacterEffect.CharacterEffectId

object ManipulatorOfObjectsImmunity {
  val metadata: CharacterEffectMetadata =
    CharacterEffectMetadata(
      name = CharacterEffectName.ManipulatorOfObjectsImmunity,
      initialEffectType = CharacterEffectType.Positive,
      description = "This character is immune to Manipulator of Objects.",
      isCc = true,
    )
}

case class ManipulatorOfObjectsImmunity(effectId: CharacterEffectId, initialCooldown: Int) extends CharacterEffect(effectId) {
  val metadata: CharacterEffectMetadata = ManipulatorOfObjectsImmunity.metadata
}
