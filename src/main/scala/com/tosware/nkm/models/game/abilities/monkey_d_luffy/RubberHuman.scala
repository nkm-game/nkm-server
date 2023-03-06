package com.tosware.nkm.models.game.abilities.monkey_d_luffy

import com.tosware.nkm.NkmConf
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.ability.Ability.AbilityId
import com.tosware.nkm.models.game.ability._
import com.tosware.nkm.models.game.character.NkmCharacter.CharacterId
import com.tosware.nkm.models.game.event.{GameEvent, GameEventListener}

import scala.util.Random

object RubberHuman {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Rubber Human",
      alternateName = "ゴム人間 (Gomu Ningen)",
      abilityType = AbilityType.Passive,
      description =
        "Character reduces {rangedDamageReductionPercent}% damage from basic attacks coming from ranged characters.",
      variables = NkmConf.extract("abilities.monkey_d_luffy.rubberHuman"),
    )
}

case class RubberHuman(abilityId: AbilityId, parentCharacterId: CharacterId)
  extends Ability(abilityId, parentCharacterId)
    with GameEventListener {
  override val metadata: AbilityMetadata = RubberHuman.metadata

  override def onEvent(e: GameEvent.GameEvent)(implicit random: Random, gameState: GameState): GameState = {
    e match {
      case GameEvent.CharacterPreparedToAttack(_, _, _, _, characterId, targetCharacterId) =>
        if(targetCharacterId != parentCharacter.id) return gameState
        val incomingAttackType = gameState.characterById(characterId).state.attackType
        gameState
      case _ =>
        gameState
    }
  }
}
