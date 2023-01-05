package com.tosware.nkm.models.game.effects

import com.tosware.nkm.models.game.CharacterEffect.CharacterEffectId
import com.tosware.nkm.models.game.{CharacterEffect, CharacterEffectMetadata, CharacterEffectName, CharacterEffectType}

object Fly {
  val metadata: CharacterEffectMetadata =
  CharacterEffectMetadata(
    name = CharacterEffectName.Fly,
    initialEffectType = CharacterEffectType.Positive,
    description = "This characterOpt can fly, allowing them to pass walls and enemy characters.",
  )
}

case class Fly(effectId: CharacterEffectId, initialCooldown: Int) extends CharacterEffect(effectId) {
  val metadata: CharacterEffectMetadata = Fly.metadata
}
