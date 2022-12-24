package com.tosware.nkm.models.game.effects

import com.tosware.nkm.models.Damage
import com.tosware.nkm.models.game.CharacterEffect.CharacterEffectId
import com.tosware.nkm.models.game.GameEvent.{CharacterTookAction, TurnFinished}
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.hex.HexUtils._

import scala.util.Random

object Poison {
  def metadata: CharacterEffectMetadata =
    CharacterEffectMetadata(
      name = CharacterEffectName.Poison,
      initialEffectType = CharacterEffectType.Negative,
      description =
        """Poison.
          |Deals damage at the end of turn.""".stripMargin,
    )
}

object MurasamePoison {
  def metadata: CharacterEffectMetadata =
    CharacterEffectMetadata(
      name = CharacterEffectName.MurasamePoison,
      initialEffectType = CharacterEffectType.Negative,
      description =
      """Poison.
        |Deals damage at the end of turn.
        |Character will be killed when fully stacked.""".stripMargin,
    )
}

case class Poison(effectId: CharacterEffectId, initialCooldown: Int, damage: Damage, metadata: CharacterEffectMetadata = Poison.metadata)
  extends CharacterEffect(effectId)
    with GameEventListener {
  override def onEvent(e: GameEvent.GameEvent)(implicit random: Random, gameState: GameState): GameState =
    e match {
      case TurnFinished(_) =>
        val characterIdThatTookAction =
          gameState.gameLog.events
            .ofType[CharacterTookAction]
            .inTurn(e.turn.number)
            .head
            .characterId
        if(characterIdThatTookAction == parentCharacter.id) {
          gameState.damageCharacter(parentCharacter.id, damage)(random, id)
        } else gameState
      case _ => gameState
    }
}
