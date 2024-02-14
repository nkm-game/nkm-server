package com.tosware.nkm.models.game.effects

import com.tosware.nkm.*
import com.tosware.nkm.models.game.character_effect.*
import com.tosware.nkm.models.game.game_state.GameState

object Ground {
  val metadata: CharacterEffectMetadata =
    CharacterEffectMetadata(
      name = CharacterEffectName.Ground,
      initialEffectType = CharacterEffectType.Negative,
      description = "Cannot move.",
      isCc = true,
    )
}

case class Ground(effectId: CharacterEffectId, initialCooldown: Int) extends CharacterEffect(effectId) {
  val metadata: CharacterEffectMetadata = Ground.metadata

  override def description(implicit gameState: GameState): String =
    metadata.description
}
