package com.tosware.nkm.models.game.effects

import com.tosware.nkm.models.game.character_effect.CharacterEffect.CharacterEffectId
import com.tosware.nkm.models.game.character_effect._

object Block {
  val metadata: CharacterEffectMetadata =
    CharacterEffectMetadata(
      name = CharacterEffectName.Block,
      initialEffectType = CharacterEffectType.Positive,
      description = "This character blocks next basic attack.",
      isCc = true,
    )
}

case class Block(effectId: CharacterEffectId, initialCooldown: Int) extends CharacterEffect(effectId) {
  val metadata: CharacterEffectMetadata = Block.metadata
}
