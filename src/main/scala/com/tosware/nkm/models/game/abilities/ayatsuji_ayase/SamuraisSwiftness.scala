package com.tosware.nkm.models.game.abilities.ayatsuji_ayase

import com.tosware.nkm.*
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.ability.*
import com.tosware.nkm.models.game.character.StatType
import com.tosware.nkm.models.game.effects.StatBuff
import com.tosware.nkm.models.game.event.GameEvent.*
import com.tosware.nkm.models.game.event.{GameEvent, GameEventListener}

import scala.util.Random

object SamuraisSwiftness extends NkmConf.AutoExtract {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Samurai's Swiftness",
      abilityType = AbilityType.Passive,
      description =
        """After dealing damage, gain {speedPercent}% Speed buff for 1t.
          |This effect does not stack.""".stripMargin,
      relatedEffectIds = Seq(StatBuff.metadata.id),
    )
}

case class SamuraisSwiftness(abilityId: AbilityId, parentCharacterId: CharacterId)
    extends Ability(abilityId) with GameEventListener {
  override val metadata: AbilityMetadata = SamuraisSwiftness.metadata

  override def onEvent(e: GameEvent.GameEvent)(implicit random: Random, gameState: GameState): GameState =
    e match {
      case CharacterDamaged(_, _, _, causedById, _, _) =>
        val causedByCharacterIdOpt = gameState.backtrackCauseToCharacterId(causedById)
        if (causedByCharacterIdOpt.isEmpty) return gameState
        val causedByCharacterId = causedByCharacterIdOpt.get
        if (causedByCharacterId == parentCharacterId)
          gameState.setAbilityEnabled(abilityId, newEnabled = true)
        else
          gameState
      case TurnStarted(_, _, _, _, _) =>
        if (state.isEnabled) {
          gameState
            .addEffect(
              parentCharacterId,
              StatBuff(
                randomUUID(),
                1,
                StatType.Speed,
                (metadata.variables("speedPercent") * parentCharacter.state.speed) / 100,
              ),
            )(random, id)
            .setAbilityEnabled(abilityId, newEnabled = false)
        } else gameState
      case _ => gameState
    }
}
