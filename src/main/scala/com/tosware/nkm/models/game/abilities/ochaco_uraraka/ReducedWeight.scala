package com.tosware.nkm.models.game.abilities.ochaco_uraraka

import com.tosware.nkm._
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.abilities.ochaco_uraraka.ZeroGravity.applyZeroGravity
import com.tosware.nkm.models.game.ability._
import com.tosware.nkm.models.game.character.StatType
import com.tosware.nkm.models.game.hex.HexCoordinates

import scala.util.Random

object ReducedWeight {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Reduced Weight",
      abilityType = AbilityType.Normal,
      description =
        """Character applies Zero Gravity effect on a friendly character.
          |Additionally, doubles speed of target for {speedBuffDuration}t.
          |
          |Range: basic attack range""".stripMargin,
      variables = NkmConf.extract("abilities.ochaco_uraraka.reducedWeight"),
    )
}

case class ReducedWeight(abilityId: AbilityId, parentCharacterId: CharacterId)
  extends Ability(abilityId, parentCharacterId)
    with UsableOnCharacter {
  override val metadata: AbilityMetadata = ReducedWeight.metadata

  override def rangeCellCoords(implicit gameState: GameState): Set[HexCoordinates] =
    parentCharacter.basicAttackCellCoords

  override def targetsInRange(implicit gameState: GameState): Set[HexCoordinates] =
    rangeCellCoords.whereFriendsOfC(parentCharacterId)

  override def use(target: CharacterId, useData: UseData)(implicit random: Random, gameState: GameState): GameState = {
    val speedIncrease = gameState.characterById(target).state.speed

    applyZeroGravity(target, gameState)(random, id)
      .addEffect(target, effects.StatBuff(randomUUID(), metadata.variables("speedBuffDuration"), StatType.Speed, speedIncrease))(random, id)
  }
}
