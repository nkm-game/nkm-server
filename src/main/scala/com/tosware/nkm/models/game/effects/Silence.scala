package com.tosware.nkm.models.game.effects

import com.tosware.nkm.*
import com.tosware.nkm.models.game.character_effect.*
import com.tosware.nkm.models.game.game_state.GameState

object Silence {
  val metadata: CharacterEffectMetadata =
    CharacterEffectMetadata(
      name = CharacterEffectName.Silence,
      initialEffectType = CharacterEffectType.Negative,
      description = "Cannot use abilities.",
      isCc = true,
    )
}

case class Silence(effectId: CharacterEffectId, initialCooldown: Int) extends CharacterEffect(effectId) {
  val metadata: CharacterEffectMetadata = Silence.metadata

  override def description(implicit gameState: GameState): String =
    metadata.description
}
