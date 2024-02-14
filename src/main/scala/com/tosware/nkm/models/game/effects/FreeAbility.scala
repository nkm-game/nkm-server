package com.tosware.nkm.models.game.effects

import com.tosware.nkm.*
import com.tosware.nkm.models.game.character_effect.*
import com.tosware.nkm.models.game.game_state.GameState

object FreeAbility {
  val metadata: CharacterEffectMetadata =
    CharacterEffectMetadata(
      name = CharacterEffectName.FreeAbility,
      initialEffectType = CharacterEffectType.Positive,
      description = "You can use one ability for free.",
    )
}

case class FreeAbility(effectId: CharacterEffectId, initialCooldown: Int) extends CharacterEffect(effectId) {
  val metadata: CharacterEffectMetadata = FreeAbility.metadata

  override def description(implicit gameState: GameState): String =
    metadata.description
}
