package com.tosware.nkm.models.game.abilities.sinon

import com.tosware.nkm.*
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.ability.*
import com.tosware.nkm.models.game.game_state.GameState
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
      targetsMetadata = Seq(AbilityTargetMetadata.SingleCharacter),
    )
}

case class PreciseShot(abilityId: AbilityId, parentCharacterId: CharacterId)
    extends Ability(abilityId) with Usable {
  override val metadata: AbilityMetadata = PreciseShot.metadata
  override def rangeCellCoords(implicit gameState: GameState): Set[HexCoordinates] =
    parentCell.get.coordinates.getCircle(metadata.variables("range")).whereExists
  override def targetsInRange(implicit gameState: GameState): Set[HexCoordinates] =
    rangeCellCoords.whereSeenEnemiesOfC(parentCharacterId)
  override def use(useData: UseData)(implicit random: Random, gameState: GameState): GameState = {
    val target = useData.firstAsCharacterId
    gameState
      .abilityHitCharacter(id, target)
      .damageCharacter(target, Damage(DamageType.Physical, metadata.variables("damage")))(random, id)
  }
  override def useChecks(implicit useData: UseData, gameState: GameState): Set[UseCheck] =
    super.useChecks
      ++ characterBaseUseChecks(useData.firstAsCharacterId)
      + UseCheck.Character.IsEnemy(useData.firstAsCharacterId)
}
