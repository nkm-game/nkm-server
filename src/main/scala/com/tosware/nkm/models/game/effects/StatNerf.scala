package com.tosware.nkm.models.game.effects

import com.tosware.nkm.*
import com.tosware.nkm.models.game.character.StatType
import com.tosware.nkm.models.game.character_effect.*
import com.tosware.nkm.models.game.effects.StatNerf.{statTypeKey, statValueKey}
import com.tosware.nkm.models.game.game_state.GameState

import scala.util.Random

object StatNerf {
  val metadata: CharacterEffectMetadata =
    CharacterEffectMetadata(
      name = CharacterEffectName.StatNerf,
      initialEffectType = CharacterEffectType.Negative,
      description = "A certain stat is nerfed.",
    )

  val statTypeKey: String = "statType"
  val statValueKey: String = "statValue"
}

case class StatNerf(effectId: CharacterEffectId, initialCooldown: Int, statType: StatType, value: Int)
    extends CharacterEffect(effectId) {
  val metadata: CharacterEffectMetadata = StatNerf.metadata

  override def onInit()(implicit random: Random, gameState: GameState): GameState =
    gameState
      .setEffectVariable(id, statTypeKey, statType)
      .setEffectVariable(id, statValueKey, value)

  override def description(implicit gameState: GameState): String =
    "Nerf {statType} by {statValue}."
}
