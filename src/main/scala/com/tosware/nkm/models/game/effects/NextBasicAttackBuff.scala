package com.tosware.nkm.models.game.effects

import com.tosware.nkm.*
import com.tosware.nkm.models.game.character.StatType
import com.tosware.nkm.models.game.character_effect.*
import com.tosware.nkm.models.game.effects.NextBasicAttackBuff.adBuffKey
import com.tosware.nkm.models.game.event.{GameEvent, GameEventListener}
import com.tosware.nkm.models.game.game_state.GameState

import scala.util.Random

object NextBasicAttackBuff {
  def metadata: CharacterEffectMetadata =
    CharacterEffectMetadata(
      name = CharacterEffectName.NextBasicAttackBuff,
      initialEffectType = CharacterEffectType.Positive,
      description = "Next basic attack is buffed.",
      isCc = true,
    )

  val adBuffKey: String = "adBuff"
}

case class NextBasicAttackBuff(effectId: CharacterEffectId, initialCooldown: Int, adBuff: Int)
    extends CharacterEffect(effectId)
    with GameEventListener {
  val metadata: CharacterEffectMetadata = NextBasicAttackBuff.metadata
  val eid = randomUUID()(new Random())

  override def onEvent(e: GameEvent.GameEvent)(implicit random: Random, gameState: GameState): GameState =
    e match {
      case GameEvent.EffectAddedToCharacter(_, _, eid, _) =>
        if (effectId == eid)
          return gameState.setEffectVariable(id, adBuffKey, adBuff.toString)
        gameState
      case GameEvent.CharacterPreparedToAttack(_, characterId, _) =>
        if (characterId != parentCharacter.id) return gameState
        gameState.addEffect(characterId, StatBuff(eid, 1, StatType.AttackPoints, adBuff))(random, id)
      case GameEvent.CharacterBasicAttacked(_, characterId, _) =>
        if (characterId != parentCharacter.id) return gameState
        gameState
          .removeEffect(eid)(random, id)
          .removeEffect(id)(random, id)
      case _ => gameState
    }
}
