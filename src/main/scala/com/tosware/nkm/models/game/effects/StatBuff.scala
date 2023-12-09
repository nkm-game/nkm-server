package com.tosware.nkm.models.game.effects

import com.tosware.nkm.*
import com.tosware.nkm.models.game.GameState
import com.tosware.nkm.models.game.character.StatType
import com.tosware.nkm.models.game.character_effect.*
import com.tosware.nkm.models.game.effects.StatBuff.{statTypeKey, statValueKey}
import com.tosware.nkm.models.game.event.{GameEvent, GameEventListener}

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
    extends CharacterEffect(effectId)
    with GameEventListener {
  val metadata: CharacterEffectMetadata = StatBuff.metadata

  override def onEvent(e: GameEvent.GameEvent)(implicit random: Random, gameState: GameState): GameState =
    e match {
      case GameEvent.EffectAddedToCharacter(_, _, _, _, _, eid, _) =>
        if (effectId == eid)
          return gameState
            .setEffectVariable(id, statTypeKey, statType.toString)
            .setEffectVariable(id, statValueKey, value.toString)
        gameState
      case _ => gameState
    }
}
