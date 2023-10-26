package com.tosware.nkm.models.game.effects

import com.tosware.nkm.*
import com.tosware.nkm.models.game.character_effect.*

object Invisibility {
  val metadata: CharacterEffectMetadata =
    CharacterEffectMetadata(
      name = CharacterEffectName.Invisibility,
      initialEffectType = CharacterEffectType.Positive,
      description =
        """State and position on the map are hidden.
          |Accidentally walking into this character breaks invisibility.""".stripMargin,
    )
}

case class Invisibility(effectId: CharacterEffectId, initialCooldown: Int) extends CharacterEffect(effectId) {
  val metadata: CharacterEffectMetadata = Invisibility.metadata
}
