package com.tosware.nkm.models.game.abilities.blank

import com.tosware.nkm.NkmConf
import com.tosware.nkm.models.game.Ability.{AbilityId, UseCheck}
import com.tosware.nkm.models.game.NkmCharacter.CharacterId
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.hex.HexUtils._

import scala.util.Random

object Castling {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Castling",
      abilityType = AbilityType.Ultimate,
      description = "Character swaps the posistions of 2 characters on the map.",
      variables = NkmConf.extract("abilities.blank.castling"),
    )
}

case class Castling(abilityId: AbilityId, parentCharacterId: CharacterId) extends Ability(abilityId, parentCharacterId) with UsableOnCharacter {
  override val metadata = Castling.metadata

  override def rangeCellCoords(implicit gameState: GameState) =
    gameState.hexMap.get.cells.toCoords

  override def targetsInRange(implicit gameState: GameState) =
    rangeCellCoords.whereCharacters

  override def use(target: CharacterId, useData: UseData)(implicit random: Random, gameState: GameState) = {
    val target1 = gameState.characterById(target).get
    val target2 = gameState.characterById(useData.data).get
    implicit val causedById: String = id

    gameState
      .removeCharacterFromMap(target1.id)
      .removeCharacterFromMap(target2.id)
      .placeCharacter(target2.parentCell.get.coordinates, target1.id)
      .placeCharacter(target1.parentCell.get.coordinates, target2.id)
  }

  override def useChecks(implicit target: CharacterId, useData: UseData, gameState: GameState): Set[UseCheck] = {
    val target1 = gameState.characterById(target).get
    val target2 = gameState.characterById(useData.data).get

    super.useChecks ++ Seq(
      (target1.id != target2.id) -> "Targets have to be different.",
      UseCheck.TargetCharacter.IsOnMap,
      UseCheck.TargetCharacter.IsOnMap(target2.id, useData, gameState),
    )
  }
}
