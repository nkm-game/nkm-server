package com.tosware.nkm.models.game.abilities.shana

import com.tosware.nkm._
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.ability._
import com.tosware.nkm.models.game.hex._

import scala.util.Random

object FinalBattleSecretTechnique {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Final Battle Secret Technique",
      alternateName = "決戦奥儀 (Kessen Ōgi)",
      abilityType = AbilityType.Ultimate,
      description =
        """Character uses abilities below on enemy in order:
          |
          |Shinku (真紅, True Crimson):
          |Knocks back an enemy by {trueCrimsonKnockback}
          |
          |Hien (飛焔, Blazing Flame):
          |Sends a blazing flame (width {blazingFlameWidth}) towards target.
          |Flame deals {blazingFlameDamage} magical damage and ends on the target.
          |
          |Shinpan (審判, Judgment) and Danzai (断罪, Condemnation):
          |Deals {judgementAndCondemnationDamagePerCharacter} true damage to target for every character (excluding themself) that is in range of {judgementAndCondemnationRange}.
          |
          |Range: linear, stops at walls, {range}
          |""".stripMargin,
      variables = NkmConf.extract("abilities.shana.finalBattleSecretTechnique"),
    )
}

case class FinalBattleSecretTechnique(abilityId: AbilityId, parentCharacterId: CharacterId) extends Ability(abilityId, parentCharacterId) with UsableOnCharacter {
  override val metadata: AbilityMetadata = FinalBattleSecretTechnique.metadata

  override def rangeCellCoords(implicit gameState: GameState): Set[HexCoordinates] =
    parentCell.fold(Set.empty[HexCoordinates])(
      _.getArea(metadata.variables("range"), Set(SearchFlag.StraightLine, SearchFlag.StopAtWalls)).toCoords
    )

  override def targetsInRange(implicit gameState: GameState): Set[HexCoordinates] =
    rangeCellCoords.whereEnemiesOfC(parentCharacterId)


  override def use(target: CharacterId, useData: UseData)(implicit random: Random, gameState: GameState): GameState = {
    val hitGs = gameState.abilityHitCharacter(id, target)
    val targetCoordinates = gameState.characterById(target).parentCell.get.coordinates
    val targetDirection = parentCell.get.coordinates.getDirection(targetCoordinates).get
    val lineCoords = targetCoordinates.getLine(targetDirection, metadata.variables("trueCrimsonKnockback"))
    val lineCells = lineCoords.toCells

    // TODO
    hitGs

//    if(lineCells.isEmpty)
//      return stunAndDamage(target)(random, hitGs)
//
//    val firstBlockedCell = lineCells.find(!_.isFreeToStand)
//
//    if(firstBlockedCell.isEmpty) {
//      val teleportGs =
//      if(lineCells.size < lineCoords.size)
//        return stunAndDamage(target)(random, teleportGs)
//      else
//        return teleportGs
//    }
//
//    val cellToTeleportIndex = lineCells.indexOf(firstBlockedCell.get) - 1
//    if(cellToTeleportIndex < 0)
//      return stunAndDamage(target)(random, hitGs)
//    else {
//      val cellToTeleport = lineCells(cellToTeleportIndex)
//      val teleportGs = hitGs.teleportCharacter(target, cellToTeleport.coordinates)(random, id)
//      return stunAndDamage(target)(random, teleportGs)
//    }
  }

}
