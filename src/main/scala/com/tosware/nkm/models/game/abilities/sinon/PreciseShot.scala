package com.tosware.nkm.models.game.abilities.sinon

import com.tosware.nkm.NkmConf
import com.tosware.nkm.models.game.Ability.{AbilityId, UseCheck}
import com.tosware.nkm.models.game.NkmCharacter.CharacterId
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.hex.HexCoordinates
import com.tosware.nkm.models.{Damage, DamageType}

import scala.util.Random

object PreciseShot {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Precise Shot",
      abilityType = AbilityType.Ultimate,
      description =
        """Character shoots enemy dealing {damage} physical damage.
          |
          |Range: circular, {range}
          |""".stripMargin,
      variables = NkmConf.extract("abilities.sinon.preciseShot"),
    )
}

case class PreciseShot(abilityId: AbilityId, parentCharacterId: CharacterId) extends Ability(abilityId, parentCharacterId) with UsableOnCharacter {
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
