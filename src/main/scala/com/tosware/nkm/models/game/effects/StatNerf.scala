package com.tosware.nkm.models.game.effects

import com.tosware.nkm.models.game.CharacterEffect.CharacterEffectId
import com.tosware.nkm.models.game._

object StatNerf {
  val metadata: CharacterEffectMetadata =
    CharacterEffectMetadata(
      name = CharacterEffectName.StatNerf,
      initialEffectType = CharacterEffectType.Negative,
      description = "Nerfs a certain stat in character.",
    )
}

case class StatNerf(effectId: CharacterEffectId, initialCooldown: Int, statType: StatType, value: Int) extends CharacterEffect(effectId) {
  val metadata: CharacterEffectMetadata = StatNerf.metadata
}
