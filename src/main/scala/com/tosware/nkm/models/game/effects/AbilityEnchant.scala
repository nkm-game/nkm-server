package com.tosware.nkm.models.game.effects

import com.tosware.nkm.*
import com.tosware.nkm.models.game.ability.AbilityType
import com.tosware.nkm.models.game.character_effect.*
import com.tosware.nkm.models.game.effects.AbilityEnchant.abilityTypeKey
import com.tosware.nkm.models.game.game_state.GameState

import scala.util.Random

object AbilityEnchant {
  val metadata: CharacterEffectMetadata =
    CharacterEffectMetadata(
      name = CharacterEffectName.AbilityEnchant,
      initialEffectType = CharacterEffectType.Positive,
      description = "Buff abilities of a certain type.",
    )

  val abilityTypeKey: String = "abilityType"
}

case class AbilityEnchant(effectId: CharacterEffectId, initialCooldown: Int, abilityType: AbilityType)
    extends CharacterEffect(effectId) {
  val metadata: CharacterEffectMetadata = AbilityEnchant.metadata

  override def onInit()(implicit random: Random, gameState: GameState): GameState =
    gameState
      .setEffectVariable(id, abilityTypeKey, abilityType)

  override def description(implicit gameState: GameState): String =
    s"Buff an ability of {abilityType} type."
}
