package com.tosware.NKM.models.game.abilities.roronoa_zoro

import com.tosware.NKM.NKMConf
import com.tosware.NKM.models.game.Ability.AbilityId
import com.tosware.NKM.models.game.NKMCharacter.CharacterId
import com.tosware.NKM.models.game._
import com.tosware.NKM.models.game.hex.HexUtils._
import com.tosware.NKM.models.game.hex.{HexCell, SearchFlag}

import scala.util.Random

object OneHundredEightPoundPhoenix {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "108 Pound Phoenix",
      alternateName = "百八煩悩鳳",
      abilityType = AbilityType.Ultimate,
      description = "Character sends 3 shockwaves towards the target enemy, each dealing 18 physical damage",
      cooldown = NKMConf.int("abilities.roronoaZoro.oneHundredEightPoundPhoenix.cooldown"),
    )
  val damage: Int = NKMConf.int("abilities.roronoaZoro.oneHundredEightPoundPhoenix.damage")
}

case class OneHundredEightPoundPhoenix(abilityId: AbilityId, parentCharacterId: CharacterId) extends Ability(abilityId) with UsableOnCharacter {
  override val metadata = OneHundredEightPoundPhoenix.metadata
  override val state = AbilityState(parentCharacterId)
  override def rangeCellCoords(implicit gameState: GameState) = {
    parentCell.fold(Set.empty[HexCell])(_.getArea(
      metadata.range,
      Set(SearchFlag.StopAtWalls, SearchFlag.StopAfterEnemies, SearchFlag.StraightLine),
      friendlyPlayerIdOpt = Some(parentCharacter.owner.id),

    ))
    gameState.hexMap.get.getSpawnPointsFor(parentCharacter.owner.id).map(_.coordinates)
  }

  override def targetsInRange(implicit gameState: GameState) =
    rangeCellCoords.whereEmpty

  override def use(target: CharacterId, useData: UseData)(implicit random: Random, gameState: GameState): GameState = ???
}
