package com.tosware.nkm.models.game.abilities.liones_elizabeth

import com.tosware.nkm.*
import com.tosware.nkm.models.game.ability.*
import com.tosware.nkm.models.game.event.{GameEvent, GameEventListener}
import com.tosware.nkm.models.game.game_state.GameState

import scala.util.Random

object ImmenseHealingPowers extends NkmConf.AutoExtract {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Immense Healing Powers",
      abilityType = AbilityType.Passive,
      description =
        """Healing is stronger based on the target's missing HP.
          |Above {firstThreshold}%  missing HP - {firstThresholdHealing}% stronger healing
          |Above {secondThreshold}% missing HP - {secondThresholdHealing}% stronger healing""".stripMargin,
    )
}

case class ImmenseHealingPowers(abilityId: AbilityId, parentCharacterId: CharacterId)
    extends Ability(abilityId)
    with GameEventListener {
  override val metadata: AbilityMetadata = ImmenseHealingPowers.metadata
  override def onEvent(e: GameEvent.GameEvent)(implicit random: Random, gameState: GameState): GameState = e match {
    case GameEvent.HealPrepared(context, targetCharacterId, amount) =>
      val additionalHealing = for {
        causedByCharacter <- gameState.backtrackCauseToCharacterId(context.causedById)
        if causedByCharacter == parentCharacterId
      } yield {
        val targetCharacter = gameState.characterById(targetCharacterId)
        val targetMissingHpPercent = targetCharacter.state.missingHpPercent
        targetMissingHpPercent match {
          case x if x > metadata.variables("secondThreshold") =>
            amount * metadata.variables("secondThresholdHealing") - amount
          case x if x > metadata.variables("firstThreshold") =>
            amount * metadata.variables("firstThresholdHealing") - amount
          case _ => 0
        }
      }
      additionalHealing match {
        case Some(healing) => gameState.amplifyHeal(context.id, healing)(random, id)
        case None          => gameState
      }
    case _ => gameState
  }
}
