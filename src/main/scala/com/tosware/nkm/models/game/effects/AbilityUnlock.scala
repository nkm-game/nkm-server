package com.tosware.nkm.models.game.effects

import com.tosware.nkm.*
import com.tosware.nkm.models.game.character_effect.*
import com.tosware.nkm.models.game.game_state.GameState

object AbilityUnlock {
  val metadata: CharacterEffectMetadata =
    CharacterEffectMetadata(
      name = CharacterEffectName.AbilityUnlock,
      initialEffectType = CharacterEffectType.Positive,
      description = "You can use an ability that is not on cooldown regardless of its state.",
    )
}

case class AbilityUnlock(effectId: CharacterEffectId, initialCooldown: Int) extends CharacterEffect(effectId) {
  val metadata: CharacterEffectMetadata = AbilityUnlock.metadata

  override def description(implicit gameState: GameState): String =
    metadata.description
}
