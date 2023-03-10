package com.tosware.nkm.models.game.effects

import com.tosware.nkm._
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.character.StatType
import com.tosware.nkm.models.game.character_effect._
import com.tosware.nkm.models.game.effects.StatNerf.{statTypeKey, statValueKey}
import com.tosware.nkm.models.game.event.{GameEvent, GameEventListener}

import scala.util.Random

object StatNerf {
  val metadata: CharacterEffectMetadata =
    CharacterEffectMetadata(
      name = CharacterEffectName.StatNerf,
      initialEffectType = CharacterEffectType.Negative,
      description = "Nerfs a certain stat in character.",
    )

  val statTypeKey: String = "statType"
  val statValueKey: String = "statValue"
}

case class StatNerf(effectId: CharacterEffectId, initialCooldown: Int, statType: StatType, value: Int)
  extends CharacterEffect(effectId)
    with GameEventListener
{
  val metadata: CharacterEffectMetadata = StatNerf.metadata

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
