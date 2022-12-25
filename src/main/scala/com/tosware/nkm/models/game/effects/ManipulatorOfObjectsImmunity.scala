package com.tosware.nkm.models.game.effects

import com.tosware.nkm.models.game.CharacterEffect.CharacterEffectId
import com.tosware.nkm.models.game.{CharacterEffect, CharacterEffectMetadata, CharacterEffectName, CharacterEffectType}

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
