package com.tosware.nkm.models.game.abilities.roronoa_zoro

import com.tosware.nkm.NkmConf
import com.tosware.nkm.models.game.Ability.AbilityId
import com.tosware.nkm.models.game.NkmCharacter.CharacterId
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.hex.HexUtils._
import com.tosware.nkm.models.game.hex.{HexCell, HexDirection, SearchFlag}
import com.tosware.nkm.models.{Damage, DamageType}

import scala.util.Random

object OneHundredEightPoundPhoenix {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "108 Pound Phoenix",
      alternateName = "百八煩悩鳳",
      abilityType = AbilityType.Ultimate,
      description = "Character sends 3 shockwaves towards the target enemy, each dealing 18 physical damage",
      variables = NkmConf.extract("abilities.roronoaZoro.oneHundredEightPoundPhoenix"),
    )
}

case class OneHundredEightPoundPhoenix(abilityId: AbilityId, parentCharacterId: CharacterId) extends Ability(abilityId) with UsableOnCharacter {
  override val metadata = OneHundredEightPoundPhoenix.metadata
  override val state = AbilityState(parentCharacterId)
  override def rangeCellCoords(implicit gameState: GameState) =
    parentCell.fold(Set.empty[HexCell])(_.getArea(
      metadata.variables("range"),
      Set(SearchFlag.StopAtWalls, SearchFlag.StopAfterEnemies, SearchFlag.StraightLine),
      friendlyPlayerIdOpt = Some(parentCharacter.owner.id),
    )).toCoords

  override def targetsInRange(implicit gameState: GameState) =
    rangeCellCoords.whereEnemiesOfC(parentCharacterId)

  private def sendShockwave(direction: HexDirection)(implicit random: Random, gameState: GameState): GameState = {
    val targetOpt = parentCell.get.firstCharacterInLine(direction, metadata.variables("range"), c => c.isEnemyForC(parentCharacterId))
    targetOpt.fold(gameState)(c => gameState.damageCharacter(c.id, Damage(DamageType.Physical, metadata.variables("damage")))(random, id))
  }

  override def use(target: CharacterId, useData: UseData)(implicit random: Random, gameState: GameState): GameState = {
    val targetCoordinates = gameState.characterById(target).get.parentCell.get.coordinates
    val targetDirection = parentCell.get.coordinates.getDirection(targetCoordinates).get

    val g1 = sendShockwave(targetDirection)
    val g2 = sendShockwave(targetDirection)(random, g1)
    val g3 = sendShockwave(targetDirection)(random, g2)
    g3
  }

}
