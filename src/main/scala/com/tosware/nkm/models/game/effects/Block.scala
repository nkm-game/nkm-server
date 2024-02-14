package com.tosware.nkm.models.game.effects

import com.tosware.nkm.*
import com.tosware.nkm.models.game.character_effect.*
import com.tosware.nkm.models.game.game_state.GameState

object Block {
  val metadata: CharacterEffectMetadata =
    CharacterEffectMetadata(
      name = CharacterEffectName.Block,
      initialEffectType = CharacterEffectType.Positive,
      description = "Block next basic attack.",
      isCc = true,
    )
}

case class Block(effectId: CharacterEffectId, initialCooldown: Int) extends CharacterEffect(effectId) {
  val metadata: CharacterEffectMetadata = Block.metadata

  override def description(implicit gameState: GameState): String =
    metadata.description
}
