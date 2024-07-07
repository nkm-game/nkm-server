package com.tosware.nkm.models.game.abilities.ryuko_matoi

import com.tosware.nkm.*
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.ability.*
import com.tosware.nkm.models.game.character.StatType
import com.tosware.nkm.models.game.game_state.GameState
import com.tosware.nkm.models.game.hex.*

import scala.util.Random

object FiberDecapitation extends NkmConf.AutoExtract {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Fiber Decapitation",
      abilityType = AbilityType.Normal,
      description =
        """Cut through an enemy:
          | - decrease their physical defense by {physicalDefenseDecrease}
          | - deal {damage} physical damage
          | - land {targetCellOffset} tiles behind them
          |
          |Range: linear, stops at walls and enemies, {range}""".stripMargin,
      traits = Seq(AbilityTrait.Move, AbilityTrait.ContactEnemy),
      targetsMetadata = Seq(AbilityTargetMetadata.SingleCharacter),
    )
  val tpOffset: Int = metadata.variables("targetCellOffset")
}

case class FiberDecapitation(abilityId: AbilityId, parentCharacterId: CharacterId)
    extends Ability(abilityId) with Usable {
  override val metadata: AbilityMetadata = FiberDecapitation.metadata
  override def rangeCellCoords(implicit gameState: GameState): Set[HexCoordinates] =
    TeleportThroughUtils.rangeCellCoords(this)
  override def targetsInRange(implicit gameState: GameState): Set[HexCoordinates] =
    TeleportThroughUtils.targetsInRange(this, FiberDecapitation.tpOffset)
  override def use(useData: UseData)(implicit random: Random, gameState: GameState): GameState = {
    val target = useData.firstAsCharacterId
    val targetCharacter = gameState.characterById(target)

    TeleportThroughUtils.tpCoordsOpt(this, target, FiberDecapitation.tpOffset) match {
      case Some(tpCoords) =>
        gameState
          .abilityHitCharacter(id, target)
          .setStat(
            target,
            StatType.PhysicalDefense,
            targetCharacter.state.purePhysicalDefense - metadata.variables("physicalDefenseDecrease"),
          )(random, id)
          .damageCharacter(target, Damage(DamageType.Physical, metadata.variables("damage")))(random, id)
          .teleportCharacter(parentCharacterId, tpCoords)(random, id)
      case None =>
        gameState
    }
  }
  override def useChecks(implicit useData: UseData, gameState: GameState): Set[UseCheck] = {
    val target = useData.firstAsCharacterId
    super.useChecks ++
      characterBaseUseChecks(target) ++
      Seq(
        UseCheck.Character.IsEnemy(target),
        TeleportThroughUtils.UseCheck.CellToTeleportLooksFreeToStand(this, target, FiberDecapitation.tpOffset),
      )
  }
}
