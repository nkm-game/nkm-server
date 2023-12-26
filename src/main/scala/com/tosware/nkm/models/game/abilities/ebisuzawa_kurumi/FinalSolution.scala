package com.tosware.nkm.models.game.abilities.ebisuzawa_kurumi

import com.tosware.nkm.*
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.ability.*
import com.tosware.nkm.models.game.hex.{HexCell, HexCoordinates, SearchFlag}

import scala.util.Random

object FinalSolution extends NkmConf.AutoExtract {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Final Solution",
      abilityType = AbilityType.Ultimate,
      description =
        """Brutally finish an enemy.
          |Deal {missingHpBonusDamagePercent}% missing HP physical damage and apply Bleeding effect which deals {bleedDamage} true damage over {bleedDuration}t.
          |
          |Range: linear, stop at walls and enemies, {range}
          |""".stripMargin,
      traits = Seq(AbilityTrait.ContactEnemy),
      targetsMetadata = Seq(AbilityTargetMetadata.SingleCharacter),
    )
}

case class FinalSolution(abilityId: AbilityId, parentCharacterId: CharacterId)
    extends Ability(abilityId)
    with Usable {
  override val metadata: AbilityMetadata = FinalSolution.metadata
  override def rangeCellCoords(implicit gameState: GameState): Set[HexCoordinates] =
    parentCell.fold(Set.empty[HexCell])(_.getArea(
      metadata.variables("range"),
      Set(SearchFlag.StopAtWalls, SearchFlag.StopAfterEnemies, SearchFlag.StraightLine),
    )).toCoords
  override def targetsInRange(implicit gameState: GameState): Set[HexCoordinates] =
    rangeCellCoords.whereSeenEnemiesOfC(parentCharacterId)
  override def use(useData: UseData)(implicit random: Random, gameState: GameState): GameState = {
    val target = useData.firstAsCharacterId
    val targetMissingHp = gameState.characterById(target).state.missingHp
    val damage = Damage(DamageType.Physical, targetMissingHp)
    val bleedDamage = Damage(DamageType.True, metadata.variables("bleedDamage"))
    val bleedEffect = effects.Poison(randomUUID(), metadata.variables("bleedDuration"), bleedDamage)
    hitAndDamageCharacter(target, damage)
      .addEffect(target, bleedEffect)(random, id)
  }
  override def useChecks(implicit useData: UseData, gameState: GameState): Set[UseCheck] =
    super.useChecks ++ characterBaseUseChecks(useData.firstAsCharacterId)
}
