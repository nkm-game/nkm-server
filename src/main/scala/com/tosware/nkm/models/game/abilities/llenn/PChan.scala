package com.tosware.nkm.models.game.abilities.llenn

import com.tosware.nkm.*
import com.tosware.nkm.models.game.ability.*
import com.tosware.nkm.models.game.character.StatType
import com.tosware.nkm.models.game.event.{GameEvent, GameEventListener}
import com.tosware.nkm.models.game.game_state.GameState

import scala.util.Random

object PChan extends NkmConf.AutoExtract {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "P-Chan",
      abilityType = AbilityType.Passive,
      description = "Permanently gain {speedIncrease} speed for every death of an ally.",
    )
}

case class PChan(
    abilityId: AbilityId,
    parentCharacterId: CharacterId,
) extends Ability(abilityId) with GameEventListener {
  override val metadata: AbilityMetadata = PChan.metadata
  override def onEvent(e: GameEvent.GameEvent)(implicit random: Random, gameState: GameState): GameState =
    e match {
      case GameEvent.CharacterDied(_, characterId) =>
        if (parentCharacter.isFriendForC(characterId)) {
          gameState
            .setAbilityVariable(
              id,
              "currentSpeedBonus",
              (state.variables("currentSpeedBonus").toInt + metadata.variables("speedIncrease")).toString,
            )
            .setStat(
              parentCharacterId,
              StatType.Speed,
              parentCharacter.state.pureSpeed + metadata.variables("speedIncrease"),
            )(random, id)
        } else gameState
      case _ => gameState
    }
}
