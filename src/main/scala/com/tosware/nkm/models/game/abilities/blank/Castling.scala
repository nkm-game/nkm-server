package com.tosware.nkm.models.game.abilities.blank

import com.tosware.nkm.*
import com.tosware.nkm.models.game.ability.*
import com.tosware.nkm.models.game.game_state.GameState
import com.tosware.nkm.models.game.hex.HexCoordinates

import scala.util.Random

object Castling extends NkmConf.AutoExtract {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Castling",
      abilityType = AbilityType.Ultimate,
      description = "Swap the positions of 2 characters on the map.",
      targetsMetadata = Seq(AbilityTargetMetadata(2 to 2, AbilityTargetType.Character)),
    )
}

case class Castling(abilityId: AbilityId, parentCharacterId: CharacterId) extends Ability(abilityId)
    with Usable {
  override val metadata: AbilityMetadata = Castling.metadata
  override def rangeCellCoords(implicit gameState: GameState): Set[HexCoordinates] =
    gameState.hexMap.cells.toCoords
  override def targetsInRange(implicit gameState: GameState): Set[HexCoordinates] =
    rangeCellCoords.whereCharacters
  override def use(useData: UseData)(implicit random: Random, gameState: GameState): GameState = {
    val target1 = gameState.characterById(useData.firstAsCharacterId)
    val target2 = gameState.characterById(useData.secondAsCharacterId)
    implicit val causedById: String = id

    gameState.swapCharacters(target1.id, target2.id)
  }
  override def useChecks(implicit useData: UseData, gameState: GameState): Set[UseCheck] = {
    val targetId1 = useData.firstAsCharacterId
    val targetId2 = useData.secondAsCharacterId

    super.useChecks ++ Seq(
      (targetId1 != targetId2) -> "Targets have to be different.",
      UseCheck.Character.IsOnMap(targetId1),
      UseCheck.Character.IsOnMap(targetId2),
    )
  }
}
