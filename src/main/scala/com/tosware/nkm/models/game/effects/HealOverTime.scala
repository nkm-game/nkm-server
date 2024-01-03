package com.tosware.nkm.models.game.effects

import com.tosware.nkm.*
import com.tosware.nkm.models.game.character_effect.*
import com.tosware.nkm.models.game.effects.HealOverTime.healKey
import com.tosware.nkm.models.game.event.GameEvent.TurnFinished
import com.tosware.nkm.models.game.event.{GameEvent, GameEventListener}
import com.tosware.nkm.models.game.game_state.GameState

import scala.util.Random

object HealOverTime {
  def metadata: CharacterEffectMetadata =
    CharacterEffectMetadata(
      name = CharacterEffectName.HealOverTime,
      initialEffectType = CharacterEffectType.Positive,
      description = "Heal at the end of the turn.",
    )

  val healKey: String = "heal"
}

case class HealOverTime(effectId: CharacterEffectId, initialCooldown: Int, heal: Int) extends CharacterEffect(effectId)
    with GameEventListener {
  override val metadata: CharacterEffectMetadata = HealOverTime.metadata
  override def onEvent(e: GameEvent.GameEvent)(implicit random: Random, gameState: GameState): GameState =
    e match {
      case GameEvent.EffectAddedToCharacter(_, _, eid, _) =>
        if (effectId == eid)
          return gameState.setEffectVariable(id, healKey, heal.toString)
        gameState
      case TurnFinished(_, _) =>
        val characterIdThatTookAction = gameState.gameLog.characterThatTookActionInTurn(e.context.turn.number).get
        if (characterIdThatTookAction == parentCharacter.id) {
          gameState.heal(parentCharacter.id, heal)(random, id)
        } else gameState
      case _ => gameState
    }
}
