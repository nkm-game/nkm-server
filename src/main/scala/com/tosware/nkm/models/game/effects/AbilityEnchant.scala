package com.tosware.nkm.models.game.effects

import com.tosware.nkm.*
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.ability.AbilityType
import com.tosware.nkm.models.game.character_effect.*
import com.tosware.nkm.models.game.effects.AbilityEnchant.abilityTypeKey
import com.tosware.nkm.models.game.event.{GameEvent, GameEventListener}

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
    extends CharacterEffect(effectId)
    with GameEventListener {
  val metadata: CharacterEffectMetadata = AbilityEnchant.metadata

  override def onEvent(e: GameEvent.GameEvent)(implicit random: Random, gameState: GameState): GameState =
    e match {
      case GameEvent.EffectAddedToCharacter(_, _, _, _, eid, _) =>
        if (effectId == eid)
          return gameState
            .setEffectVariable(id, abilityTypeKey, abilityType.toString)
        gameState
      case _ => gameState
    }
}
