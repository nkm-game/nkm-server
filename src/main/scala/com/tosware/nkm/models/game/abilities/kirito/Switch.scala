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
      targetsMetadata = Seq(AbilityTargetMetadata.SingleCharacter),
    )
}

case class Switch(abilityId: AbilityId, parentCharacterId: CharacterId) extends Ability(abilityId)
    with Usable {
  override val metadata: AbilityMetadata = Switch.metadata
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
  override def use(useData: UseData)(implicit random: Random, gameState: GameState): GameState = {
    val target1 = gameState.characterById(parentCharacterId)
    val target2 = gameState.characterById(useData.firstAsCharacterId)
    implicit val causedById: String = id

    gameState
      .swapCharacters(target1.id, target2.id)
      .refreshAnything(parentCharacterId)
  }

  override def useChecks(implicit useData: UseData, gameState: GameState): Set[UseCheck] = {
    val target = useData.firstAsCharacterId
    super.useChecks ++ characterBaseUseChecks(target) + UseCheck.Character.IsFriend(target)
  }
}
