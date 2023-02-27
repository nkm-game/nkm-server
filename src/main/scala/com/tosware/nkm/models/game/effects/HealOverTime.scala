package com.tosware.nkm.models.game.effects

import com.tosware.nkm.models.game.character_effect.CharacterEffect.CharacterEffectId
import com.tosware.nkm.models.game.event.GameEvent.TurnFinished
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.character_effect.{CharacterEffect, CharacterEffectMetadata, CharacterEffectName, CharacterEffectType}
import com.tosware.nkm.models.game.effects.HealOverTime.healKey
import com.tosware.nkm.models.game.event.{GameEvent, GameEventListener}

import scala.util.Random

object HealOverTime {
  def metadata: CharacterEffectMetadata =
    CharacterEffectMetadata(
      name = CharacterEffectName.HealOverTime,
      initialEffectType = CharacterEffectType.Positive,
      description =
        """Heals at the end of turn.""".stripMargin,
    )

  val healKey: String = "heal"
}

case class HealOverTime(effectId: CharacterEffectId, initialCooldown: Int, heal: Int) extends CharacterEffect(effectId)
    with GameEventListener {
  override val metadata: CharacterEffectMetadata = HealOverTime.metadata
  override def onEvent(e: GameEvent.GameEvent)(implicit random: Random, gameState: GameState): GameState =
    e match {
      case GameEvent.EffectAddedToCharacter(_, _, _, _, eid, _) =>
        if(effectId == eid)
          return gameState.setEffectVariable(id, healKey, heal.toString)
        gameState
      case TurnFinished(_, _, _, _) =>
        val characterIdThatTookAction = gameState.gameLog.characterThatTookActionInTurn(e.turn.number).get
        if(characterIdThatTookAction == parentCharacter.id) {
          gameState.heal(parentCharacter.id, heal)(random, id)
        } else gameState
      case _ => gameState
    }
}
