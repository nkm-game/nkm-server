package com.tosware.nkm.models.game.abilities.shana

import com.tosware.nkm.*
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.ability.*
import com.tosware.nkm.models.game.hex.*

import scala.util.Random

object FinalBattleSecretTechnique extends NkmConf.AutoExtract {
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
          |Flame deals {blazingFlameDamage} magical damage to enemies and ends on the target.
          |
          |Shinpan (審判, Judgment) and Danzai (断罪, Condemnation):
          |Deals {judgementAndCondemnationDamagePerCharacter} true damage to target for every character (excluding themself) that is in range of {judgementAndCondemnationRange}.
          |
          |Range: linear, stops at walls, {range}
          |""".stripMargin,

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
    val direction = gameState.getDirection(parentCharacterId, target).get

    val (knockbackGs, _) =
      gameState
        .abilityHitCharacter(id, target)
        .knockbackCharacter(target, direction, metadata.variables("trueCrimsonKnockback"))(random, id)

    val flameGs = (for {
      targetCoords: HexCoordinates <- knockbackGs.hexMap.getCellOfCharacter(target).map(_.coordinates)
      parentCoords: HexCoordinates <- parentCell(knockbackGs).map(_.coordinates)

      blazingFlameCoords: Seq[HexCoordinates] = parentCoords.getThickLine(targetCoords, metadata.variables("blazingFlameWidth"))
      flameTargets = blazingFlameCoords.whereEnemiesOfC(parentCharacterId).characters.map(_.id)
      damage = Damage(DamageType.Magical, metadata.variables("blazingFlameDamage"))
      flameGs = flameTargets.foldLeft(knockbackGs)((acc, cid) => hitAndDamageCharacter(cid, damage)(random, acc))
    } yield flameGs).getOrElse(knockbackGs)

    val judgementRange = metadata.variables("judgementAndCondemnationRange")
    val condemnationDamagePerCharacter = metadata.variables("judgementAndCondemnationDamagePerCharacter")

    val condemnationGs = (for {
      parentCoords: HexCoordinates <- parentCell(flameGs).map(_.coordinates)
      judgementMultiplier = parentCoords.getCircle(judgementRange).whereCharacters.size - 1
      damage = Damage(DamageType.True, condemnationDamagePerCharacter * judgementMultiplier)
      condemnationGs = hitAndDamageCharacter(target, damage)(random, flameGs)
    } yield condemnationGs) .getOrElse(flameGs)

    condemnationGs
  }
}
