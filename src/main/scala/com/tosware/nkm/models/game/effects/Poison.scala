package com.tosware.nkm.models.game.effects

import com.tosware.nkm.models.game.character_effect.CharacterEffect.CharacterEffectId
import com.tosware.nkm.models.game.GameEvent.TurnFinished
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.character_effect.{CharacterEffect, CharacterEffectMetadata, CharacterEffectName, CharacterEffectType}
import com.tosware.nkm.models.game.effects.Poison.damageKey
import spray.json._

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

  val damageKey: String = "damage"
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
      case GameEvent.EffectAddedToCharacter(_, _, _, _, eid, _) =>
        if(effectId == eid)
          return gameState.setEffectVariable(id, damageKey, damage.toJson.toString)
        gameState
      case TurnFinished(_, _, _, _) =>
        val characterIdThatTookAction = gameState.gameLog.characterThatTookActionInTurn(e.turn.number).get
        if(characterIdThatTookAction == parentCharacter.id) {
          gameState.damageCharacter(parentCharacter.id, damage)(random, id)
        } else gameState
      case _ => gameState
    }
}
