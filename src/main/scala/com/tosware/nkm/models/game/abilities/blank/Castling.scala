package com.tosware.nkm.models.game.abilities.blank

import com.tosware.nkm.*
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.ability.*

import scala.util.Random

object Castling {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Castling",
      abilityType = AbilityType.Ultimate,
      description = "Character swaps the positions of 2 characters on the map.",
      variables = NkmConf.extract("abilities.blank.castling"),
    )
}

case class Castling(abilityId: AbilityId, parentCharacterId: CharacterId) extends Ability(abilityId, parentCharacterId) with UsableOnCharacter {
  override val metadata = Castling.metadata

  override def rangeCellCoords(implicit gameState: GameState) =
    gameState.hexMap.cells.toCoords

  override def targetsInRange(implicit gameState: GameState) =
    rangeCellCoords.whereCharacters

  override def use(target: CharacterId, useData: UseData)(implicit random: Random, gameState: GameState) = {
    val target1 = gameState.characterById(target)
    val target2 = gameState.characterById(useData.data)
    implicit val causedById: String = id

    gameState
      .removeCharacterFromMap(target1.id)
      .removeCharacterFromMap(target2.id)
      .placeCharacter(target2.parentCell.get.coordinates, target1.id)
      .placeCharacter(target1.parentCell.get.coordinates, target2.id)
  }

  override def useChecks(implicit target: CharacterId, useData: UseData, gameState: GameState): Set[UseCheck] = {
    val target1 = gameState.characterById(target)
    val target2 = gameState.characterById(useData.data)

    super.useChecks ++ Seq(
      (target1.id != target2.id) -> "Targets have to be different.",
      UseCheck.TargetCharacter.IsOnMap,
      UseCheck.TargetCharacter.IsOnMap(target2.id, useData, gameState),
    )
  }
}
