package com.tosware.nkm.models.game.abilities.blank

import com.tosware.nkm.NkmConf
import com.tosware.nkm.models.game.Ability.AbilityId
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

  override def use(target: CharacterId, useData: UseData)(implicit random: Random, gameState: GameState) = ???
}
