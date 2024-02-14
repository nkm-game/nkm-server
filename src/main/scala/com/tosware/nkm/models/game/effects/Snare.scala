package com.tosware.nkm.models.game.effects

import com.tosware.nkm.*
import com.tosware.nkm.models.game.character_effect.*
import com.tosware.nkm.models.game.game_state.GameState

object Snare {
  val metadata: CharacterEffectMetadata =
    CharacterEffectMetadata(
      name = CharacterEffectName.Snare,
      initialEffectType = CharacterEffectType.Negative,
      description = "Cannot basic move.",
      isCc = true,
    )
}

case class Snare(effectId: CharacterEffectId, initialCooldown: Int) extends CharacterEffect(effectId) {
  val metadata: CharacterEffectMetadata = Snare.metadata

  override def description(implicit gameState: GameState): String =
    metadata.description
}
