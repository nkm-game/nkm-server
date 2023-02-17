package com.tosware.nkm.models.game.effects

import com.tosware.nkm.models.game.character_effect.{CharacterEffect, CharacterEffectMetadata, CharacterEffectName, CharacterEffectType}
import com.tosware.nkm.models.game.character_effect.CharacterEffect.CharacterEffectId
import com.tosware.nkm.models.game.effects.StatBuff.{statTypeKey, statValueKey}
import com.tosware.nkm.models.game.{GameEvent, GameEventListener, GameState, StatType}

import scala.util.Random

object StatBuff {
  val metadata: CharacterEffectMetadata =
    CharacterEffectMetadata(
      name = CharacterEffectName.StatBuff,
      initialEffectType = CharacterEffectType.Positive,
      description = "Buffs a certain stat in character.",
    )

  val statTypeKey: String = "statType"
  val statValueKey: String = "statValue"
}

case class StatBuff(effectId: CharacterEffectId, initialCooldown: Int, statType: StatType, value: Int)
  extends CharacterEffect(effectId)
    with GameEventListener
{
  val metadata: CharacterEffectMetadata = StatBuff.metadata

  override def onEvent(e: GameEvent.GameEvent)(implicit random: Random, gameState: GameState): GameState =
    e match {
      case GameEvent.EffectAddedToCharacter(_, _, _, _, eid, _) =>
        if(effectId == eid)
          return gameState
            .setEffectVariable(id, statTypeKey, statType.toString)
            .setEffectVariable(id, statValueKey, value.toString)
        gameState
      case _ => gameState
    }
}
