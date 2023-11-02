package com.tosware.nkm.models.game.abilities.kirito

import com.tosware.nkm.*
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.ability.*
import com.tosware.nkm.models.game.hex.HexCoordinates

import scala.util.Random

object Switch extends NkmConf.AutoExtract {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Switch",
      abilityType = AbilityType.Normal,
      description =
        """Switch places with selected ally.
          |You or your ally have to be in a basic attack range of an enemy.
          |You can use basic attack or another ability just after switching.
          |
          |Range: circular, {range}""".stripMargin,
      relatedEffectIds = Seq(effects.AbilityUnlock.metadata.id),
      traits = Seq(AbilityTrait.Move),
    )
}

case class Switch(abilityId: AbilityId, parentCharacterId: CharacterId) extends Ability(abilityId, parentCharacterId)
    with UsableOnCharacter {
  override val metadata = Switch.metadata

  override def rangeCellCoords(implicit gameState: GameState): Set[HexCoordinates] = {
    if (parentCell.isEmpty) return Set.empty
    val rangeCoords = parentCell.get.coordinates.getCircle(metadata.variables("range")) - parentCell.get.coordinates
    val enemiesAaCoords = gameState
      .players.filterNot(_.name == parentCharacter.owner.id)
      .flatMap(_.characterIds)
      .map(gameState.characterById)
      .flatMap(_.basicAttackCellCoords)
      .toSet

    if (enemiesAaCoords.contains(parentCell.get.coordinates)) rangeCoords
    else rangeCoords.intersect(enemiesAaCoords)
  }

  override def targetsInRange(implicit gameState: GameState): Set[HexCoordinates] =
    rangeCellCoords.whereFriendsOfC(parentCharacterId)

  override def use(target: CharacterId, useData: UseData)(implicit random: Random, gameState: GameState) = {
    val target1 = gameState.characterById(parentCharacterId)
    val target2 = gameState.characterById(target)
    implicit val causedById: String = id

    gameState
      .removeCharacterFromMap(target1.id)
      .removeCharacterFromMap(target2.id)
      .placeCharacter(target2.parentCell.get.coordinates, target1.id)
      .placeCharacter(target1.parentCell.get.coordinates, target2.id)
      .refreshAnything(parentCharacterId)
  }
}
