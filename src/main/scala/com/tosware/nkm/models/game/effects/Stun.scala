package com.tosware.nkm.models.game.effects

import com.tosware.nkm.*
import com.tosware.nkm.models.game.character_effect.*
import com.tosware.nkm.models.game.game_state.GameState

object Stun {
  val metadata: CharacterEffectMetadata =
    CharacterEffectMetadata(
      name = CharacterEffectName.Stun,
      initialEffectType = CharacterEffectType.Negative,
      description = "Cannot take action.",
      isCc = true,
    )
}

case class Stun(effectId: CharacterEffectId, initialCooldown: Int) extends CharacterEffect(effectId) {
  val metadata: CharacterEffectMetadata = Stun.metadata

  override def description(implicit gameState: GameState): String =
    metadata.description
}
