package com.tosware.nkm.models.game.abilities.ayatsuji_ayase

import com.tosware.nkm.NkmConf
import com.tosware.nkm.models.game.GameState
import com.tosware.nkm.models.game.ability.Ability.{AbilityId, UseCheck}
import com.tosware.nkm.models.game.ability._
import com.tosware.nkm.models.game.character.NkmCharacter.CharacterId
import com.tosware.nkm.models.game.hex.HexCoordinates
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.hex_effect.HexCellEffectName
import spray.json._

import scala.util.Random

object CrackTheSky {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Crack the Sky",
      abilityType = AbilityType.Normal,
      description =
        "Character detonates selected traps, dealing {damage}+B physical damage to all hit enemies.",
      variables = NkmConf.extract("abilities.ayatsuji_ayase.crackTheSky"),
    )
}

case class CrackTheSky(abilityId: AbilityId, parentCharacterId: CharacterId)
  extends Ability(abilityId, parentCharacterId)
    with Usable
{
  override val metadata: AbilityMetadata = MarkOfTheWind.metadata

  override def rangeCellCoords(implicit gameState: GameState): Set[HexCoordinates] =
    gameState.hexMap.cells.toCoords

  override def targetsInRange(implicit gameState: GameState): Set[HexCoordinates] =
    rangeCellCoords
      .toCells
      .filter(_.effects.exists(_.metadata.name == HexCellEffectName.MarkOfTheWind))
      .toCoords

  override def use(useData: UseData)(implicit random: Random, gameState: GameState): GameState = {
    val targetCoords = useData.data.parseJson.convertTo[Seq[HexCoordinates]]
    val trapEffectIds = targetCoords.toCells.flatMap(_.effects.ofType[hex_effects.MarkOfTheWind]).map(_.id)

    gameState.removeHexCellEffects(trapEffectIds)(random, id)
  }

  override def useChecks(implicit useData: UseData, gameState: GameState): Set[UseCheck] = {
    val targetCoords = useData.data.parseJson.convertTo[Seq[HexCoordinates]]
    super.useChecks ++ Seq(
      (targetCoords.toCells.size == targetCoords.size) ->
        "Not all selected coordinates exist.",
      targetCoords.toCells.forall(_.effects.ofType[hex_effects.MarkOfTheWind].nonEmpty) ->
        "Not all selected coordinates have traps on them."
    )
  }

}