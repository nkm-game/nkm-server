package com.tosware.nkm.models.game.abilities.hecate

import com.tosware.nkm.*
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.ability.*
import com.tosware.nkm.models.game.hex.HexCoordinates

import scala.util.Random

object Aster extends NkmConf.AutoExtract {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Aster",
      alternateName = "星 (Asuteru)",
      abilityType = AbilityType.Normal,
      description =
        """Shoot rays of energy, dealing {damage} magical damage to hit enemies.
          |
          |Range: circular, {range}
          |Radius: circular, {radius}""".stripMargin,
      targetsMetadata = Seq(AbilityTargetMetadata.CircularAirSelection),
    )
}

case class Aster(abilityId: AbilityId, parentCharacterId: CharacterId) extends Ability(abilityId) with Usable {
  override val metadata: AbilityMetadata = Aster.metadata
  override def rangeCellCoords(implicit gameState: GameState): Set[HexCoordinates] =
    parentCell.get.coordinates.getCircle(metadata.variables("range")).whereExists
  override def targetsInRange(implicit gameState: GameState): Set[HexCoordinates] =
    rangeCellCoords
  override def use(useData: UseData)(implicit
      random: Random,
      gameState: GameState,
  ): GameState = {
    val targets =
      useData.firstAsCoordinates
        .getCircle(metadata.variables("radius"))
        .whereSeenEnemiesOfC(parentCharacterId)
        .characters.map(_.id)
    val damage = Damage(DamageType.Magical, metadata.variables("damage"))
    targets.foldLeft(gameState)((acc, cid) => hitAndDamageCharacter(cid, damage)(random, acc))
  }
  override def useChecks(implicit useData: UseData, gameState: GameState): Set[UseCheck] =
    super.useChecks ++ coordinatesBaseUseChecks(useData.firstAsCoordinates)
}
