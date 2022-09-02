package com.tosware.nkm.models.game.abilities.roronoa_zoro

import com.tosware.nkm.NkmConf
import com.tosware.nkm.models.game.Ability.{AbilityId, UseCheck}
import com.tosware.nkm.models.game.NkmCharacter.CharacterId
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.hex.HexUtils._
import com.tosware.nkm.models.game.hex.{HexCell, SearchFlag}

import scala.util.Random

object OgreCutter {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Ogre Cutter",
      alternateName = "鬼斬り",
      abilityType = AbilityType.Normal,
      description = "Character deals basic damage to selected target in range and teleports 2 tiles behind it.",
      variables = NkmConf.extract("abilities.roronoaZoro.ogreCutter"),
    )
}

case class OgreCutter(abilityId: AbilityId, parentCharacterId: CharacterId) extends Ability(abilityId) with UsableOnCharacter {
  override val metadata = OgreCutter.metadata
  override val state = AbilityState(parentCharacterId)
  override def rangeCellCoords(implicit gameState: GameState) =
    parentCell.fold(Set.empty[HexCell])(c => c.getArea(
      metadata.variables("range"),
      Set(SearchFlag.StopAtWalls, SearchFlag.StopAfterEnemies, SearchFlag.StraightLine),
      friendlyPlayerIdOpt = Some(parentCharacter.owner.id),
    )).toCoords

  override def targetsInRange(implicit gameState: GameState) =
    rangeCellCoords.whereEnemiesOfC(parentCharacterId)

  override def use(target: CharacterId, useData: UseData)(implicit random: Random, gameState: GameState) = {
    val targetCoordinates = gameState.characterById(target).get.parentCell.get.coordinates
    val targetDirection = parentCell.get.coordinates.getDirection(targetCoordinates).get
    val teleportCoordinates = targetCoordinates.getInDirection(targetDirection, 2)
    gameState
      .abilityHitCharacter(id, target)
      .basicAttack(parentCharacterId, target)
      .teleportCharacter(parentCharacterId, teleportCoordinates)(random, id)
  }

  override def useChecks(implicit target: CharacterId, useData: UseData, gameState: GameState): Set[UseCheck] = {
    def cellToTeleportIsFreeToStand(): Boolean = {
      val targetCharacter: NkmCharacter = gameState.characterById(target).get
      val targetCoordinatesOpt = targetCharacter.parentCell.map(_.coordinates)
      if (targetCoordinatesOpt.isEmpty) return false
      val targetDirectionOpt = parentCell.get.coordinates.getDirection(targetCoordinatesOpt.get)
      if (targetDirectionOpt.isEmpty) return false
      val teleportCoordinates = targetCoordinatesOpt.get.getInDirection(targetDirectionOpt.get, 2)
      if (!teleportCoordinates.toCell(gameState.hexMap.get).isFreeToStand) return false
      true
    }

    super.useChecks ++ Seq(
      UseCheck.TargetIsEnemy,
      cellToTeleportIsFreeToStand() -> "Cell to teleport is not free to stand."
    )
  }
}