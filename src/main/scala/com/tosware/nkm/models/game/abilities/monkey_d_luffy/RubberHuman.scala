package com.tosware.nkm.models.game.abilities.monkey_d_luffy

import com.tosware.nkm.*
import com.tosware.nkm.models.game.ability.*
import com.tosware.nkm.models.game.character.AttackType
import com.tosware.nkm.models.game.event.{GameEvent, GameEventListener}
import com.tosware.nkm.models.game.game_state.GameState

import scala.util.Random

object RubberHuman extends NkmConf.AutoExtract {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Rubber Human",
      alternateName = "ゴム人間 (Gomu Ningen)",
      abilityType = AbilityType.Passive,
      description =
        "Reduce {rangedDamageReductionPercent}% damage from basic attacks coming from ranged characters.",
    )
}

case class RubberHuman(abilityId: AbilityId, parentCharacterId: CharacterId)
    extends Ability(abilityId)
    with GameEventListener {
  override val metadata: AbilityMetadata = RubberHuman.metadata
  override def onEvent(e: GameEvent.GameEvent)(implicit random: Random, gameState: GameState): GameState =
    e match {
      case GameEvent.DamagePrepared(_, _, damage) =>
        val eventIndex = e.index
        if (eventIndex == 0)
          return gameState
        val lastEvent = gameState.gameLog.events(eventIndex - 1)
        lastEvent match {
          case GameEvent.CharacterPreparedToAttack(_, characterId, targetCharacterId) =>
            if (targetCharacterId != parentCharacter.id) return gameState
            val incomingAttackType = gameState.characterById(characterId).state.attackType
            if (incomingAttackType != AttackType.Ranged)
              return gameState
            val damageAmountReduction = damage.amount * metadata.variables("rangedDamageReductionPercent") / 100
            gameState.amplifyDamage(e.id, -damageAmountReduction)(random, id)
          case _ =>
            gameState
        }
      case _ =>
        gameState
    }
}
