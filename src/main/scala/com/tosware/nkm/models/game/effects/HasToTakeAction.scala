package com.tosware.nkm.models.game.effects

import com.tosware.nkm.*
import com.tosware.nkm.models.game.character_effect.*
import com.tosware.nkm.models.game.event.GameEvent
import com.tosware.nkm.models.game.event.GameEvent.TurnStarted
import com.tosware.nkm.models.game.game_state.GameState

import scala.util.Random

object HasToTakeAction {
  val metadata: CharacterEffectMetadata =
    CharacterEffectMetadata(
      name = CharacterEffectName.HasToTakeAction,
      initialEffectType = CharacterEffectType.Negative,
      description = "Has to take action next turn.",
      isCc = true,
    )
}

case class HasToTakeAction(effectId: CharacterEffectId, initialCooldown: Int)
    extends CharacterEffect(effectId) {
  val metadata: CharacterEffectMetadata = HasToTakeAction.metadata

  override def onEventReceived(e: GameEvent.GameEvent)(implicit random: Random, gameState: GameState): GameState =
    e match {
      case TurnStarted(_, _) =>
        if (gameState.currentPlayer.id == parentCharacter.owner.id) {
          gameState.takeActionWithCharacter(parentCharacter.id)
        } else gameState
      case _ => gameState
    }

  override def description(implicit gameState: GameState): String =
    metadata.description
}
