package com.tosware.nkm.models.game.effects

import com.tosware.nkm.models.game.CharacterEffect.CharacterEffectId
import com.tosware.nkm.models.game._

import scala.util.Random

object ApplyEffectOnBasicAttack {
  def metadata: CharacterEffectMetadata =
    CharacterEffectMetadata(
      name = CharacterEffectName.ApplyEffectOnBasicAttack,
      initialEffectType = CharacterEffectType.Positive,
      description = "Applies an effect on next basic attack's target.",
      isCc = true,
    )
}

case class ApplyEffectOnBasicAttack(effectId: CharacterEffectId, initialCooldown: Int, effectToApply: CharacterEffect)
  extends CharacterEffect(effectId)
    with GameEventListener {
  val metadata: CharacterEffectMetadata = ApplyEffectOnBasicAttack.metadata

  override def onEvent(e: GameEvent.GameEvent)(implicit random: Random, gameState: GameState): GameState =
    e match {
      case GameEvent.CharacterBasicAttacked(_, _, _, _, characterId, targetCharacterId) =>
        if(characterId != parentCharacter.id) return gameState
        gameState
          .addEffect(targetCharacterId, effectToApply)(random, id)
          .removeEffect(id)(random, id)
      case _ => gameState
    }
}
