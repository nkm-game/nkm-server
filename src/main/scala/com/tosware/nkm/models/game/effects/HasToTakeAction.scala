package com.tosware.nkm.models.game.effects

import com.tosware.nkm.*
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.character_effect.*
import com.tosware.nkm.models.game.event.GameEvent.TurnStarted
import com.tosware.nkm.models.game.event.{GameEvent, GameEventListener}

import scala.util.Random

object HasToTakeAction {
  val metadata: CharacterEffectMetadata =
    CharacterEffectMetadata(
      name = CharacterEffectName.HasToTakeAction,
      initialEffectType = CharacterEffectType.Negative,
      description = "This character has to take action next turn.",
      isCc = true,
    )
}

case class HasToTakeAction(effectId: CharacterEffectId, initialCooldown: Int)
  extends CharacterEffect(effectId)
  with GameEventListener
  {
  val metadata: CharacterEffectMetadata = HasToTakeAction.metadata

    override def onEvent(e: GameEvent.GameEvent)(implicit random: Random, gameState: GameState): GameState = {
      e match {
        case TurnStarted(_, _, _, _) =>
          if(gameState.currentPlayer.id == parentCharacter.owner.id) {
            gameState.takeActionWithCharacter(parentCharacter.id)
          } else gameState
        case _ => gameState
      }
    }

  }
