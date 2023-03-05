package com.tosware.nkm.models.game.abilities.nibutani_shinka

import com.tosware.nkm.NkmConf
import com.tosware.nkm.models.game.ability.Ability.AbilityId
import com.tosware.nkm.models.game.character.NkmCharacter.CharacterId
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.ability.{Ability, AbilityMetadata, AbilityType, UsableOnCharacter, UseData}
import com.tosware.nkm.models.game.hex._

import scala.util.Random

object SummerBreeze {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Summer Breeze",
      abilityType = AbilityType.Normal,
      description =
        """Character summons Summer Breeze that knocks back selected enemy by {knockback}.
          |If the enemy will be knocked back into a wall or another character,
          |they will be stunned for {stunDuration}t and receive {damage} magical damage.
          |
          |Range: linear, {range}
          |""".stripMargin,
      variables = NkmConf.extract("abilities.nibutani_shinka.summerBreeze"),
      relatedEffectIds = Seq(effects.Stun.metadata.id),
    )
}

case class SummerBreeze(abilityId: AbilityId, parentCharacterId: CharacterId) extends Ability(abilityId, parentCharacterId) with UsableOnCharacter {
  override val metadata: AbilityMetadata = SummerBreeze.metadata

  override def rangeCellCoords(implicit gameState: GameState): Set[HexCoordinates] =
    parentCell.fold(Set.empty[HexCoordinates])(
      _.getArea(metadata.variables("range"), Set(SearchFlag.StraightLine)).toCoords
    )

  override def targetsInRange(implicit gameState: GameState): Set[HexCoordinates] =
    rangeCellCoords.whereEnemiesOfC(parentCharacterId)


  private def stunAndDamage(target: CharacterId)(implicit random: Random, gameState: GameState): GameState = {
    gameState
      .addEffect(target, effects.Stun(randomUUID(), metadata.variables("stunDuration")))(random, id)
      .damageCharacter(target, Damage(DamageType.Magical, metadata.variables("damage")))(random, id)
  }


  override def use(target: CharacterId, useData: UseData)(implicit random: Random, gameState: GameState): GameState = {
    val hitGs = gameState.abilityHitCharacter(id, target)
    val targetCoordinates = gameState.characterById(target).parentCell.get.coordinates
    val targetDirection = parentCell.get.coordinates.getDirection(targetCoordinates).get
    val lineCoords = targetCoordinates.getLine(targetDirection, metadata.variables("knockback"))
    val lineCells = lineCoords.toCells

    if(lineCells.isEmpty)
      return stunAndDamage(target)(random, hitGs)

    val firstBlockedCell = lineCells.find(!_.isFreeToStand)

    if(firstBlockedCell.isEmpty) {
      val teleportGs = hitGs.teleportCharacter(target, lineCells.last.coordinates)(random, id)
      if(lineCells.size < lineCoords.size)
        return stunAndDamage(target)(random, teleportGs)
      else
        return teleportGs
    }

    val cellToTeleportIndex = lineCells.indexOf(firstBlockedCell.get) - 1
    if(cellToTeleportIndex < 0)
      return stunAndDamage(target)(random, hitGs)
    else {
      val cellToTeleport = lineCells(cellToTeleportIndex)
      val teleportGs = hitGs.teleportCharacter(target, cellToTeleport.coordinates)(random, id)
      return stunAndDamage(target)(random, teleportGs)
    }
  }

}
