package com.tosware.nkm.models.game.effects

import com.tosware.nkm.*
import com.tosware.nkm.models.game.character_effect.*
import com.tosware.nkm.models.game.effects.HealOverTime.healKey
import com.tosware.nkm.models.game.event.GameEvent
import com.tosware.nkm.models.game.event.GameEvent.TurnFinished
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

case class HealOverTime(effectId: CharacterEffectId, initialCooldown: Int, heal: Int)
    extends CharacterEffect(effectId) {
  override val metadata: CharacterEffectMetadata = HealOverTime.metadata
  override def onInit()(implicit random: Random, gameState: GameState): GameState =
    gameState.setEffectVariable(id, healKey, heal)
  override def onEventReceived(e: GameEvent.GameEvent)(implicit random: Random, gameState: GameState): GameState =
    e match {
      case TurnFinished(_, _) =>
        val characterIdThatTookAction = gameState.gameLog.characterThatTookActionInTurn(e.context.turn.number).get
        if (characterIdThatTookAction == parentCharacter.id) {
          gameState.heal(parentCharacter.id, heal)(random, id)
        } else gameState
      case _ => gameState
    }

  override def description(implicit gameState: GameState): String =
    "Heal {heal} HP at the end of the turn."
}
