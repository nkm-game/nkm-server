package com.tosware.nkm.models.game.abilities.roronoa_zoro

import com.tosware.nkm.*
import com.tosware.nkm.models.game.ability.*
import com.tosware.nkm.models.game.game_state.GameState
import com.tosware.nkm.models.game.hex.*

import scala.util.Random

object OgreCutter extends NkmConf.AutoExtract {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Ogre Cutter",
      alternateName = "鬼斬り (Oni Giri)",
      abilityType = AbilityType.Normal,
      description =
        """Basic attack an enemy and teleport {targetCellOffset} tiles behind them.
          |
          |Range: linear, stops at walls and enemies, {range}""".stripMargin,
      traits = Seq(AbilityTrait.Move, AbilityTrait.ContactEnemy),
      targetsMetadata = Seq(AbilityTargetMetadata.SingleCharacter),
    )
  val tpOffset: Int = metadata.variables("targetCellOffset")
}

case class OgreCutter(abilityId: AbilityId, parentCharacterId: CharacterId)
    extends Ability(abilityId) with Usable {
  override val metadata: AbilityMetadata = OgreCutter.metadata
  override def rangeCellCoords(implicit gameState: GameState): Set[HexCoordinates] =
    TeleportThroughUtils.rangeCellCoords(this)
  override def targetsInRange(implicit gameState: GameState): Set[HexCoordinates] =
    TeleportThroughUtils.targetsInRange(this, OgreCutter.tpOffset)
  override def use(useData: UseData)(implicit random: Random, gameState: GameState): GameState = {
    val target = useData.firstAsCharacterId
    TeleportThroughUtils.tpCoordsOpt(this, target, OgreCutter.tpOffset) match {
      case Some(tpCoords) =>
        gameState
          .abilityHitCharacter(id, target)
          .basicAttack(parentCharacterId, target)
          .teleportCharacter(parentCharacterId, tpCoords)(random, id)
      case None =>
        gameState
    }
  }
  override def useChecks(implicit useData: UseData, gameState: GameState): Set[UseCheck] = {
    val target = useData.firstAsCharacterId
    super.useChecks
      ++ characterBaseUseChecks(target)
      ++ Seq(
        UseCheck.Character.IsEnemy(target),
        TeleportThroughUtils.UseCheck.CellToTeleportLooksFreeToStand(this, target, OgreCutter.tpOffset),
      )
  }
}
