package com.tosware.nkm.models.game.effects

import com.tosware.nkm.*
import com.tosware.nkm.models.game.character_effect.*
import com.tosware.nkm.models.game.effects.ApplyEffectOnBasicAttack.effectToApplyKey
import com.tosware.nkm.models.game.event.GameEvent
import com.tosware.nkm.models.game.game_state.GameState

import scala.util.Random

object ApplyEffectOnBasicAttack {
  def metadata: CharacterEffectMetadata =
    CharacterEffectMetadata(
      name = CharacterEffectName.ApplyEffectOnBasicAttack,
      initialEffectType = CharacterEffectType.Positive,
      description = "Apply an effect on next basic attack's target.",
      isCc = true,
    )

  val effectToApplyKey: String = "effectToApply"
}

case class ApplyEffectOnBasicAttack(effectId: CharacterEffectId, initialCooldown: Int, effectToApply: CharacterEffect)
    extends CharacterEffect(effectId) {
  val metadata: CharacterEffectMetadata = ApplyEffectOnBasicAttack.metadata

  override def onInit()(implicit random: Random, gameState: GameState): GameState =
    gameState
      .setEffectVariable(id, effectToApplyKey, effectToApply.metadata.id)
      .setEffectVariable(id, "effectNameToApply", effectToApply.metadata.name)
      .setEffectVariable(id, "numberOfTurnsToApply", effectToApply.initialCooldown)

  override def onEventReceived(e: GameEvent.GameEvent)(implicit random: Random, gameState: GameState): GameState =
    e match {
      case GameEvent.CharacterBasicAttacked(_, characterId, targetCharacterId) =>
        if (characterId != parentCharacter.id) return gameState
        gameState
          .addEffect(targetCharacterId, effectToApply)(random, id)
          .removeEffect(id)(random, id)
      case _ => gameState
    }

  override def description(implicit gameState: GameState): String =
    "Apply an effect {effectNameToApply} for {numberOfTurnsToApply}t on your next basic attack."

}
