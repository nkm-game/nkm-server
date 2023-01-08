package com.tosware.nkm.models.game.effects

import com.tosware.nkm.models.game.CharacterEffect.CharacterEffectId
import com.tosware.nkm.models.game._

import scala.util.Random

object NextBasicAttackBuff {
  def metadata: CharacterEffectMetadata =
    CharacterEffectMetadata(
      name = CharacterEffectName.NextBasicAttackBuff,
      initialEffectType = CharacterEffectType.Positive,
      description = "Buffs next basic attack.",
      isCc = true,
    )
}

case class NextBasicAttackBuff(effectId: CharacterEffectId, initialCooldown: Int, adBuff: Int)
  extends CharacterEffect(effectId)
    with GameEventListener {
  val metadata: CharacterEffectMetadata = NextBasicAttackBuff.metadata
  val eid = randomUUID()(new Random()) // TODO use effect state

  override def onEvent(e: GameEvent.GameEvent)(implicit random: Random, gameState: GameState): GameState =
    e match {
      case GameEvent.CharacterPreparedToAttack(_, characterId, _) =>
        if(characterId != parentCharacter.id) return gameState
        gameState.addEffect(characterId, StatBuff(eid, 1, StatType.AttackPoints, adBuff))(random, id)
      case GameEvent.CharacterBasicAttacked(_, characterId, _) =>
        if(characterId != parentCharacter.id) return gameState
        gameState
          .removeEffect(eid)(random, id)
          .removeEffect(id)(random, id)
      case _ => gameState
    }
}
