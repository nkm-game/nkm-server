package com.tosware.nkm.models.game.effects

import com.tosware.nkm.*
import com.tosware.nkm.models.game.character_effect.*
import com.tosware.nkm.models.game.game_state.GameState

object ManipulatorOfObjectsImmunity {
  val metadata: CharacterEffectMetadata =
    CharacterEffectMetadata(
      name = CharacterEffectName.ManipulatorOfObjectsImmunity,
      initialEffectType = CharacterEffectType.Positive,
      description = "Immune to Manipulator of Objects.",
      isCc = true,
    )
}

case class ManipulatorOfObjectsImmunity(effectId: CharacterEffectId, initialCooldown: Int)
    extends CharacterEffect(effectId) {
  val metadata: CharacterEffectMetadata = ManipulatorOfObjectsImmunity.metadata

  override def description(implicit gameState: GameState): String =
    metadata.description
}
