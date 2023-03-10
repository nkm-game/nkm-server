package com.tosware.nkm.models.game.abilities.ebisuzawa_kurumi

import com.tosware.nkm._
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.ability._
import com.tosware.nkm.models.game.hex.{HexCoordinates, SearchFlag}

import scala.util.Random

object FinalSolution {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Final Solution",
      abilityType = AbilityType.Ultimate,
      description =
        """Character brutally finishes the enemy, dealing {missingHpBonusDamagePercent}% missing HP physical damage and applying bleeding effect dealing {bleedDamage} true damage over {bleedDuration}t.
          |
          |Range: linear, {range}
          |""".stripMargin,
      variables = NkmConf.extract("abilities.ebisuzawa_kurumi.finalSolution"),
    )
}

case class FinalSolution(abilityId: AbilityId, parentCharacterId: CharacterId)
  extends Ability(abilityId, parentCharacterId)
    with UsableOnCharacter {
  override val metadata = FinalSolution.metadata

  override def rangeCellCoords(implicit gameState: GameState): Set[HexCoordinates] =
    parentCell.get.getArea(metadata.variables("range"), Set(SearchFlag.StraightLine)).toCoords

  override def targetsInRange(implicit gameState: GameState): Set[HexCoordinates] =
    rangeCellCoords.whereEnemiesOfC(parentCharacterId)

  override def use(target: CharacterId, useData: UseData)(implicit random: Random, gameState: GameState): GameState = {
    val targetMissingHp = gameState.characterById(target).state.missingHp
    val damage = Damage(DamageType.Physical, targetMissingHp)
    val bleedDamage = Damage(DamageType.True, metadata.variables("bleedDamage"))
    val bleedEffect = effects.Poison(randomUUID(), metadata.variables("bleedDuration"), bleedDamage)

    hitAndDamageCharacter(target, damage)
      .addEffect(target, bleedEffect)(random, id)
  }
}
