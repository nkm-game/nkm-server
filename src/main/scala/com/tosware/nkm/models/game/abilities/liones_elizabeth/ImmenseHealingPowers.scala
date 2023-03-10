package com.tosware.nkm.models.game.abilities.liones_elizabeth

import com.tosware.nkm._
import com.tosware.nkm.models.game.GameState
import com.tosware.nkm.models.game.ability._
import com.tosware.nkm.models.game.event.{GameEvent, GameEventListener}

import scala.util.Random

object ImmenseHealingPowers {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Immense Healing Powers",
      abilityType = AbilityType.Passive,
      description =
        """Healing of this character is stronger based on target missing HP:
          |above {firstTreshold}%  missing HP - {firstTresholdHealing}% stronger healing
          |above {secondTreshold}% missing HP - {secondTresholdHealing}% stronger healing
          |""".stripMargin,
      variables = NkmConf.extract("abilities.liones_elizabeth.immenseHealingPowers"),
    )
}

case class ImmenseHealingPowers(abilityId: AbilityId, parentCharacterId: CharacterId)
  extends Ability(abilityId, parentCharacterId)
  with GameEventListener
  {
  override val metadata = ImmenseHealingPowers.metadata

    override def onEvent(e: GameEvent.GameEvent)(implicit random: Random, gameState: GameState): GameState = e match {
      case GameEvent.HealPrepared(healPreparedId, _, _, causedById, targetCharacterId, amount) =>
        val causedByCharacterOpt = gameState.backtrackCauseToCharacterId(causedById)

        if(causedByCharacterOpt.isEmpty) return gameState

        val causedByCharacter: CharacterId = causedByCharacterOpt.get
        if(causedByCharacter != parentCharacterId) return gameState

        val targetCharacter = gameState.characterById(targetCharacterId)
        val targetMissingHpPercent = targetCharacter.state.missingHpPercent

        val additionalHealing = targetMissingHpPercent match {
          case x if x > metadata.variables("secondTreshold") =>
            amount * metadata.variables("secondTresholdHealing") - amount
          case x if x > metadata.variables("firstTreshold") =>
            amount * metadata.variables("firstTresholdHealing") - amount
          case _ => 0
        }

        gameState.amplifyHeal(healPreparedId, additionalHealing)(random, id)

      case _ => gameState
    }
  }
