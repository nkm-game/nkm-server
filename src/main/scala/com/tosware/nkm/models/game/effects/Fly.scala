package com.tosware.nkm.models.game.effects

import com.tosware.nkm.models.game.character_effect.{CharacterEffect, CharacterEffectMetadata, CharacterEffectName, CharacterEffectType}
import com.tosware.nkm.models.game.character_effect.CharacterEffect.CharacterEffectId

object Fly {
  val metadata: CharacterEffectMetadata =
  CharacterEffectMetadata(
    name = CharacterEffectName.Fly,
    initialEffectType = CharacterEffectType.Positive,
    description = "This character can fly, allowing them to pass walls and enemy characters.",
  )
}

case class Fly(effectId: CharacterEffectId, initialCooldown: Int) extends CharacterEffect(effectId) {
  val metadata: CharacterEffectMetadata = Fly.metadata
}
