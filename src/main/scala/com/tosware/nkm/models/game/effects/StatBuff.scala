package com.tosware.nkm.models.game.effects

import com.tosware.nkm.*
import com.tosware.nkm.models.game.character.StatType
import com.tosware.nkm.models.game.character_effect.*
import com.tosware.nkm.models.game.effects.StatBuff.{statTypeKey, statValueKey}
import com.tosware.nkm.models.game.game_state.GameState

import scala.util.Random

object StatBuff {
  val metadata: CharacterEffectMetadata =
    CharacterEffectMetadata(
      name = CharacterEffectName.StatBuff,
      initialEffectType = CharacterEffectType.Positive,
      description = "A certain stat is buffed.",
    )

  val statTypeKey: String = "statType"
  val statValueKey: String = "statValue"
}

case class StatBuff(effectId: CharacterEffectId, initialCooldown: Int, statType: StatType, value: Int)
    extends CharacterEffect(effectId) {
  val metadata: CharacterEffectMetadata = StatBuff.metadata

  override def onInit()(implicit random: Random, gameState: GameState): GameState =
    gameState
      .setEffectVariable(id, statTypeKey, statType)
      .setEffectVariable(id, statValueKey, value)

  override def description(implicit gameState: GameState): String =
    "Buff {statType} by {statValue}."
}
