package com.tosware.nkm.models.game.abilities.liones_elizabeth

import com.tosware.nkm._
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.ability._
import com.tosware.nkm.models.game.hex.HexCoordinates

import scala.util.Random

object Invigorate {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Invigorate",
      abilityType = AbilityType.Normal,
      description =
        """Character casts a spell on a friendly character that heals {heal} HP for {duration}t.
          |
          |Range: circular, {range}""".stripMargin,
      variables = NkmConf.extract("abilities.liones_elizabeth.invigorate"),
      relatedEffectIds = Seq(effects.HealOverTime.metadata.id),
    )
}

case class Invigorate(abilityId: AbilityId, parentCharacterId: CharacterId) extends Ability(abilityId, parentCharacterId) with UsableOnCharacter {
  override val metadata = Invigorate.metadata

  override def rangeCellCoords(implicit gameState: GameState): Set[HexCoordinates] =
    gameState.hexMap.cells.toCoords

  override def targetsInRange(implicit gameState: GameState): Set[HexCoordinates] =
    rangeCellCoords.whereFriendsOfC(parentCharacterId)

  override def use(target: CharacterId, useData: UseData)(implicit random: Random, gameState: GameState): GameState = {
    gameState
      .addEffect(target, effects.HealOverTime(randomUUID(), metadata.variables("duration"), metadata.variables("heal")))(random, id)
  }

  override def useChecks(implicit target: CharacterId, useData: UseData, gameState: GameState): Set[UseCheck] = {
    super.useChecks ++ Seq(
      UseCheck.TargetCharacter.IsFriend,
    )
  }
}
