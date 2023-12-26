package com.tosware.nkm.models.game.abilities.llenn

import com.tosware.nkm.*
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.ability.*
import com.tosware.nkm.models.game.game_state.GameState
import com.tosware.nkm.models.game.hex.HexCoordinates

import scala.util.Random

object GrenadeThrow extends NkmConf.AutoExtract {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Grenade Throw",
      abilityType = AbilityType.Normal,
      description =
        """Throw a grenade that deals {damage} physical damage to everyone hit.
          |
          |Range: circular, {range}
          |Radius: circular, {radius}""".stripMargin,
      targetsMetadata = Seq(AbilityTargetMetadata.CircularAirSelection),
    )
}

case class GrenadeThrow(abilityId: AbilityId, parentCharacterId: CharacterId)
    extends Ability(abilityId) with Usable {
  override val metadata: AbilityMetadata = GrenadeThrow.metadata
  override def rangeCellCoords(implicit gameState: GameState): Set[HexCoordinates] =
    parentCell.get.coordinates.getCircle(metadata.variables("range")).whereExists
  override def targetsInRange(implicit gameState: GameState): Set[HexCoordinates] =
    rangeCellCoords
  override def use(useData: UseData)(implicit
      random: Random,
      gameState: GameState,
  ): GameState = {
    val targets = useData.firstAsCoordinates.getCircle(metadata.variables("radius")).characters.map(_.id)
    val damage = Damage(DamageType.Physical, metadata.variables("damage"))
    targets.foldLeft(gameState)((acc, cid) => blastCharacter(cid, damage)(random, acc))
  }
  private def blastCharacter(target: CharacterId, damage: Damage)(implicit
      random: Random,
      gameState: GameState,
  ): GameState =
    gameState
      .abilityHitCharacter(id, target)
      .damageCharacter(target, damage)(random, id)
  override def useChecks(implicit useData: UseData, gameState: GameState): Set[UseCheck] =
    super.useChecks ++ coordinatesBaseUseChecks(useData.firstAsCoordinates)
}
