package com.tosware.nkm.models.game.abilities.sinon

import com.tosware.nkm.*
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.ability.*
import com.tosware.nkm.models.game.hex.HexCoordinates

import scala.util.Random

object PreciseShot extends NkmConf.AutoExtract {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Precise Shot",
      abilityType = AbilityType.Ultimate,
      description =
        """Shoot an enemy dealing {damage} physical damage.
          |
          |Range: circular, {range}""".stripMargin,
    )
}

case class PreciseShot(abilityId: AbilityId, parentCharacterId: CharacterId)
    extends Ability(abilityId, parentCharacterId) with UsableOnCharacter {
  override val metadata = PreciseShot.metadata

  override def rangeCellCoords(implicit gameState: GameState): Set[HexCoordinates] =
    parentCell.get.coordinates.getCircle(metadata.variables("range")).whereExists

  override def targetsInRange(implicit gameState: GameState): Set[HexCoordinates] =
    rangeCellCoords.whereEnemiesOfC(parentCharacterId)

  override def use(target: CharacterId, useData: UseData)(implicit random: Random, gameState: GameState): GameState =
    gameState
      .abilityHitCharacter(id, target)
      .damageCharacter(target, Damage(DamageType.Physical, metadata.variables("damage")))(random, id)

  override def useChecks(implicit target: CharacterId, useData: UseData, gameState: GameState): Set[UseCheck] =
    super.useChecks + UseCheck.TargetCharacter.IsEnemy
}
