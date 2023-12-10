package com.tosware.nkm.models.game.abilities.roronoa_zoro

import com.tosware.nkm.*
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.ability.*
import com.tosware.nkm.models.game.character.NkmCharacter
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
}

case class OgreCutter(abilityId: AbilityId, parentCharacterId: CharacterId)
    extends Ability(abilityId) with Usable {
  override val metadata: AbilityMetadata = OgreCutter.metadata
  private def teleportCoordinates(from: HexCoordinates, direction: HexDirection) =
    from.getInDirection(direction, metadata.variables("targetCellOffset"))
  override def rangeCellCoords(implicit gameState: GameState): Set[HexCoordinates] =
    parentCell.fold(Set.empty[HexCell])(c =>
      c.getArea(
        metadata.variables("range"),
        Set(SearchFlag.StopAtWalls, SearchFlag.StopAfterEnemies, SearchFlag.StraightLine),
        friendlyPlayerIdOpt = Some(parentCharacter.owner.id),
      )
    ).toCoords
  override def targetsInRange(implicit gameState: GameState): Set[HexCoordinates] =
    rangeCellCoords.whereSeenEnemiesOfC(parentCharacterId).filter(targetCoordinates =>
      {
        for {
          pCell <- parentCell
          targetDirection: HexDirection <- pCell.coordinates.getDirection(targetCoordinates)
          tpCoords: HexCoordinates <- Some(teleportCoordinates(targetCoordinates, targetDirection))
          tpCell: HexCell <- tpCoords.toCellOpt(gameState)
          isFreeToStand: Boolean <- Some(tpCell.looksFreeToStand(parentCharacterId))
        } yield isFreeToStand
      }.getOrElse(false)
    )
  override def use(useData: UseData)(implicit random: Random, gameState: GameState): GameState = {
    val target = useData.firstAsCharacterId
    val targetCoordinates = gameState.characterById(target).parentCellOpt.get.coordinates
    val targetDirection = parentCell.get.coordinates.getDirection(targetCoordinates).get
    val tpCoords = teleportCoordinates(targetCoordinates, targetDirection)

    gameState
      .abilityHitCharacter(id, target)
      .basicAttack(parentCharacterId, target)
      .teleportCharacter(parentCharacterId, tpCoords)(random, id)
  }
  override def useChecks(implicit useData: UseData, gameState: GameState): Set[UseCheck] = {
    val target = useData.firstAsCharacterId
    def cellToTeleportIsFreeToStand(): Boolean = {
      for {
        targetCharacter: NkmCharacter <- Some(gameState.characterById(target))
        targetCoordinates: HexCoordinates <- targetCharacter.parentCellOpt.map(_.coordinates)
        targetDirection: HexDirection <- parentCell.get.coordinates.getDirection(targetCoordinates)
        tpCoords: HexCoordinates <- Some(teleportCoordinates(targetCoordinates, targetDirection))
        tpCell: HexCell <- tpCoords.toCellOpt(gameState)
        isFreeToStand: Boolean <- Some(tpCell.looksFreeToStand(parentCharacterId))
      } yield isFreeToStand
    }.getOrElse(false)
    super.useChecks ++ characterBaseUseChecks(target)
      ++ Seq(
        UseCheck.Character.IsEnemy(target),
        cellToTeleportIsFreeToStand() -> "Cell to teleport is not free to stand or does not exist.",
      )
  }
}
