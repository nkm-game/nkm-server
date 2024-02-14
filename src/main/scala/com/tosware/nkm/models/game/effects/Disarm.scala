package com.tosware.nkm.models.game.effects

import com.tosware.nkm.*
import com.tosware.nkm.models.game.character_effect.*
import com.tosware.nkm.models.game.game_state.GameState

object Disarm {
  val metadata: CharacterEffectMetadata =
    CharacterEffectMetadata(
      name = CharacterEffectName.Disarm,
      initialEffectType = CharacterEffectType.Negative,
      description = "Cannot use basic attacks.",
      isCc = true,
    )
}

case class Disarm(effectId: CharacterEffectId, initialCooldown: Int) extends CharacterEffect(effectId) {
  val metadata: CharacterEffectMetadata = Disarm.metadata

  override def description(implicit gameState: GameState): String =
    metadata.description
}
