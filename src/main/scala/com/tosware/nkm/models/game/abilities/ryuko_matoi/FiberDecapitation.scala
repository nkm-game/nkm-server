package com.tosware.nkm.models.game.abilities.ryuko_matoi

import com.tosware.nkm.NkmConf
import com.tosware.nkm.models.game.ability.Ability.{AbilityId, UseCheck}
import com.tosware.nkm.models.game.character.NkmCharacter.CharacterId
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.ability.{Ability, AbilityMetadata, AbilityType, UsableOnCharacter, UseData}
import com.tosware.nkm.models.game.character.{NkmCharacter, StatType}
import com.tosware.nkm.models.game.hex.{HexCell, SearchFlag}

import scala.util.Random

object FiberDecapitation {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Fiber Decapitation",
      abilityType = AbilityType.Normal,
      description =
        """Character cuts through selected enemy, decreasing his physical defense by {physicalDefenseDecrease},
          |dealing {damage} physical damage and landing {targetCellOffset} tiles behind him.
          |
          |Range: {range}""".stripMargin,
      variables = NkmConf.extract("abilities.ryukoMatoi.fiberDecapitation"),
    )
}

case class FiberDecapitation(abilityId: AbilityId, parentCharacterId: CharacterId) extends Ability(abilityId, parentCharacterId) with UsableOnCharacter {
  override val metadata = FiberDecapitation.metadata

  override def rangeCellCoords(implicit gameState: GameState) =
    parentCell.fold(Set.empty[HexCell])(c => c.getArea(
      metadata.variables("range"),
      Set(SearchFlag.StopAtWalls, SearchFlag.StopAfterEnemies, SearchFlag.StraightLine),
      friendlyPlayerIdOpt = Some(parentCharacter.owner.id),
    )).toCoords

  override def targetsInRange(implicit gameState: GameState) =
    rangeCellCoords.whereEnemiesOfC(parentCharacterId)

  override def use(target: CharacterId, useData: UseData)(implicit random: Random, gameState: GameState) = {
    val targetCharacter = gameState.characterById(target)
    val targetCoordinates = targetCharacter.parentCell.get.coordinates
    val targetDirection = parentCell.get.coordinates.getDirection(targetCoordinates).get
    val teleportCoordinates = targetCoordinates.getInDirection(targetDirection, metadata.variables("targetCellOffset"))
    gameState
      .abilityHitCharacter(id, target)
      .setStat(target, StatType.PhysicalDefense, targetCharacter.state.purePhysicalDefense - metadata.variables("physicalDefenseDecrease"))(random, id)
      .basicAttack(parentCharacterId, target)
      .teleportCharacter(parentCharacterId, teleportCoordinates)(random, id)
  }

  override def useChecks(implicit target: CharacterId, useData: UseData, gameState: GameState): Set[UseCheck] = {
    def cellToTeleportIsFreeToStand(): Boolean = {
      val targetCharacter: NkmCharacter = gameState.characterById(target)
      val targetCoordinatesOpt = targetCharacter.parentCell.map(_.coordinates)
      if (targetCoordinatesOpt.isEmpty) return false
      val targetDirectionOpt = parentCell.get.coordinates.getDirection(targetCoordinatesOpt.get)
      if (targetDirectionOpt.isEmpty) return false
      val teleportCoordinates = targetCoordinatesOpt.get.getInDirection(targetDirectionOpt.get, metadata.variables("targetCellOffset"))
      if (!teleportCoordinates.toCellOpt.fold(false)(_.isFreeToStand)) return false
      true
    }

    super.useChecks ++ Seq(
      UseCheck.TargetCharacter.IsEnemy,
      cellToTeleportIsFreeToStand() -> "Cell to teleport is not free to stand or does not exist."
    )
  }
}
