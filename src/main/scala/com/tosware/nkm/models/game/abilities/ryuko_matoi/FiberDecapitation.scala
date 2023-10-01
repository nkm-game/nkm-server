package com.tosware.nkm.models.game.abilities.ryuko_matoi

import com.tosware.nkm.*
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.ability.*
import com.tosware.nkm.models.game.character.{NkmCharacter, StatType}
import com.tosware.nkm.models.game.hex.{HexCell, HexCoordinates, HexDirection, SearchFlag}

import scala.util.Random

object FiberDecapitation extends NkmConf.AutoExtract {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Fiber Decapitation",
      abilityType = AbilityType.Normal,
      description =
        """Character cuts through selected enemy, decreasing his physical defense by {physicalDefenseDecrease},
          |dealing {damage} physical damage and landing {targetCellOffset} tiles behind him.
          |Range: linear, stops at walls and enemies, {range}""".stripMargin,

    )
}

case class FiberDecapitation(abilityId: AbilityId, parentCharacterId: CharacterId) extends Ability(abilityId, parentCharacterId) with UsableOnCharacter {
  override val metadata = FiberDecapitation.metadata

  private def teleportCoordinates(from: HexCoordinates, direction: HexDirection) =
    from.getInDirection(direction, metadata.variables("targetCellOffset"))

  override def rangeCellCoords(implicit gameState: GameState): Set[HexCoordinates] =
    parentCell.fold(Set.empty[HexCell])(c => c.getArea(
      metadata.variables("range"),
      Set(SearchFlag.StopAtWalls, SearchFlag.StopAfterEnemies, SearchFlag.StraightLine),
      friendlyPlayerIdOpt = Some(parentCharacter.owner.id),
    )).toCoords

  override def targetsInRange(implicit gameState: GameState): Set[HexCoordinates] =
    rangeCellCoords.whereEnemiesOfC(parentCharacterId).filter(targetCoordinates => {
      for {
        pCell <- parentCell
        targetDirection: HexDirection <- pCell.coordinates.getDirection(targetCoordinates)
        tpCoords: HexCoordinates <- Some(teleportCoordinates(targetCoordinates, targetDirection))
        tpCell: HexCell <- tpCoords.toCellOpt(gameState)
        isFreeToStand: Boolean <- Some(tpCell.isFreeToStand)
      } yield isFreeToStand
    }.getOrElse(false))

  override def use(target: CharacterId, useData: UseData)(implicit random: Random, gameState: GameState): GameState = {
    val targetCharacter = gameState.characterById(target)
    val targetCoordinates = targetCharacter.parentCell.get.coordinates
    val targetDirection = parentCell.get.coordinates.getDirection(targetCoordinates).get
    val tpCoords = teleportCoordinates(targetCoordinates, targetDirection)

    gameState
      .abilityHitCharacter(id, target)
      .setStat(target, StatType.PhysicalDefense, targetCharacter.state.purePhysicalDefense - metadata.variables("physicalDefenseDecrease"))(random, id)
      .damageCharacter(target, Damage(DamageType.Physical, metadata.variables("damage")))(random, id)
      .teleportCharacter(parentCharacterId, tpCoords)(random, id)
  }

  override def useChecks(implicit target: CharacterId, useData: UseData, gameState: GameState): Set[UseCheck] = {
    def cellToTeleportIsFreeToStand(): Boolean = {
      {
        for {
          targetCharacter: NkmCharacter <- Some(gameState.characterById(target))
          targetCoordinates: HexCoordinates <- targetCharacter.parentCell.map(_.coordinates)
          targetDirection: HexDirection <- parentCell.get.coordinates.getDirection(targetCoordinates)
          tpCoords: HexCoordinates <- Some(teleportCoordinates(targetCoordinates, targetDirection))
          tpCell: HexCell <- tpCoords.toCellOpt(gameState)
          isFreeToStand: Boolean <- Some(tpCell.isFreeToStand)
        } yield isFreeToStand
      }.getOrElse(false)
    }

    super.useChecks ++ Seq(
      UseCheck.TargetCharacter.IsEnemy,
      cellToTeleportIsFreeToStand() -> "Cell to teleport is not free to stand or does not exist."
    )
  }
}
