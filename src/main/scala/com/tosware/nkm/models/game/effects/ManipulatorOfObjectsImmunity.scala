package com.tosware.nkm.models.game.effects

import com.tosware.nkm._
import com.tosware.nkm.models.game.character_effect._

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
