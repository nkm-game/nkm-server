package com.tosware.nkm.models.game.effects

import com.tosware.nkm.models.game.CharacterEffect.CharacterEffectId
import com.tosware.nkm.models.game.{CharacterEffect, CharacterEffectMetadata, CharacterEffectName, CharacterEffectType, StatType}

object StatBuff {
  val metadata: CharacterEffectMetadata =
    CharacterEffectMetadata(
      name = CharacterEffectName.StatBuff,
      initialEffectType = CharacterEffectType.Positive,
      description = "Buffs a certain stat in character.",
    )
}

case class StatBuff(effectId: CharacterEffectId, initialCooldown: Int, statType: StatType, value: Int) extends CharacterEffect(effectId) {
  val metadata: CharacterEffectMetadata = StatBuff.metadata
}
