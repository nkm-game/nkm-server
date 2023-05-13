package com.tosware.nkm.models.game.effects

import com.tosware.nkm.*
import com.tosware.nkm.models.game.character_effect.*

object AbilityUnlock {
  val metadata: CharacterEffectMetadata =
  CharacterEffectMetadata(
    name = CharacterEffectName.AbilityUnlock,
    initialEffectType = CharacterEffectType.Positive,
    description = "This character can use one of his abilities that is not on cooldown regardless of their state.",
  )
}

case class AbilityUnlock(effectId: CharacterEffectId, initialCooldown: Int) extends CharacterEffect(effectId) {
  val metadata: CharacterEffectMetadata = AbilityUnlock.metadata
}
