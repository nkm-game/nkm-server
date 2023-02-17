package com.tosware.nkm.models.game.abilities.kirito

import com.tosware.nkm.NkmConf
import com.tosware.nkm.models.game.ability.Ability.AbilityId
import com.tosware.nkm.models.game.character.NkmCharacter.CharacterId
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.ability.{Ability, AbilityMetadata, AbilityType, UsableOnCharacter, UseData}
import com.tosware.nkm.models.game.hex.HexCoordinates

import scala.util.Random

object Switch {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Switch",
      abilityType = AbilityType.Normal,
      description =
        """Switch places with selected friend.
          |You or your friend have to be in a basic attack range of an enemy.
          |You can use basic attack or other ability just after using this ability.
          |
          |Range: circular, {range}""".stripMargin,
      variables = NkmConf.extract("abilities.kirito.switch"),
      relatedEffectIds = Seq(effects.AbilityUnlock.metadata.id),
    )
}

case class Switch(abilityId: AbilityId, parentCharacterId: CharacterId) extends Ability(abilityId, parentCharacterId) with UsableOnCharacter {
  override val metadata = Switch.metadata

  override def rangeCellCoords(implicit gameState: GameState): Set[HexCoordinates] = {
    if(parentCell.isEmpty) return Set.empty
    val rangeCoords = parentCell.get.coordinates.getCircle(metadata.variables("range"))
    val enemiesAaCoords = gameState
      .players.filterNot(_.name == parentCharacter.owner.id)
      .flatMap(_.characterIds)
      .map(gameState.characterById)
      .flatMap(_.basicAttackCellCoords)
      .toSet

    if(enemiesAaCoords.contains(parentCell.get.coordinates)) rangeCoords
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
      .refreshBasicAttack(parentCharacterId)
      .addEffect(parentCharacterId, effects.AbilityUnlock(randomUUID(), 1))
  }
}
