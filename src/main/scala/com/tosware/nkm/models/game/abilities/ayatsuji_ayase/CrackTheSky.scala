package com.tosware.nkm.models.game.abilities.ayatsuji_ayase

import com.tosware.nkm.*
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.abilities.ayatsuji_ayase.CrackTheSky.markOfTheWindAbilityIdKey
import com.tosware.nkm.models.game.ability.*
import com.tosware.nkm.models.game.event.{GameEvent, GameEventListener}
import com.tosware.nkm.models.game.game_state.{GameState, GameStatus}
import com.tosware.nkm.models.game.hex.HexCoordinates
import com.tosware.nkm.models.game.hex_effect.HexCellEffectName
import spray.json.*

import scala.util.Random

object CrackTheSky extends NkmConf.AutoExtract {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Crack the Sky",
      abilityType = AbilityType.Normal,
      description =
        """Detonate selected traps.
          |Deal {damage}+B physical damage to all hit enemies.
          |
          |Trap detonation radius: circular, {radius}""".stripMargin,
      targetsMetadata =
        Seq(AbilityTargetMetadata(1 to MarkOfTheWind.metadata.variables("trapLimit"), AbilityTargetType.HexCoordinates)),
    )

  val markOfTheWindAbilityIdKey = "markOfTheWindAbilityId"
}

case class CrackTheSky(abilityId: AbilityId, parentCharacterId: CharacterId)
    extends Ability(abilityId)
    with Usable
    with GameEventListener {
  override val metadata: AbilityMetadata = CrackTheSky.metadata

  override def rangeCellCoords(implicit gameState: GameState): Set[HexCoordinates] =
    gameState.hexMap.cells.toCoords

  override def targetsInRange(implicit gameState: GameState): Set[HexCoordinates] =
    rangeCellCoords
      .toCells
      .filter(_.effects.exists(_.metadata.name == HexCellEffectName.MarkOfTheWind))
      .toCoords

  def markAbilityId(implicit gameState: GameState): AbilityId =
    state.variables.get(markOfTheWindAbilityIdKey)
      .map(_.parseJson.convertTo[AbilityId])
      .get

  override def use(useData: UseData)(implicit random: Random, gameState: GameState): GameState = {
    val targetCoords = useData.allAsCoordinates
    val trapEffectIds = targetCoords.toCells.flatMap(_.effects.ofType[hex_effects.MarkOfTheWind]).map(_.id)

    val damage = Damage(DamageType.Physical, parentCharacter.state.attackPoints + metadata.variables("damage"))

    val targets: Seq[CharacterId] =
      targetCoords
        .flatMap(_
          .getCircle(metadata.variables("radius"))
          .whereSeenEnemiesOfC(parentCharacterId).characters.map(_.id))

    val markAbility =
      gameState
        .abilityById(markAbilityId)
        .asInstanceOf[abilities.ayatsuji_ayase.MarkOfTheWind]

    targets.foldLeft(gameState)((acc, cid) => hitAndDamageCharacter(cid, damage)(random, acc))
      .removeHexCellEffects(trapEffectIds)(random, id)
      .setAbilityVariable(
        markAbilityId,
        MarkOfTheWind.trapLocationsKey,
        markAbility.trapLocations.filterNot(location => targetCoords.contains(location)).toJson.toString,
      )
  }

  override def useChecks(implicit useData: UseData, gameState: GameState): Set[UseCheck] = {
    val targetCoords = useData.allAsCoordinates

    super.useChecks ++ targetCoords.map(UseCheck.Coordinates.ExistsOnMap) ++ Seq(
      targetCoords.toCells.forall(_.effects.ofType[hex_effects.MarkOfTheWind].nonEmpty) ->
        "Not all selected coordinates have traps on them."
    )
  }

  override def onEvent(e: GameEvent.GameEvent)(implicit random: Random, gameState: GameState): GameState =
    e match {
      case GameEvent.GameStatusUpdated(_, GameStatus.Running) =>
        val markAbilityId =
          parentCharacter.state.abilities.ofType[abilities.ayatsuji_ayase.MarkOfTheWind].head.abilityId
        gameState
          .setAbilityVariable(id, markOfTheWindAbilityIdKey, markAbilityId.toJson.toString)
      case _ => gameState
    }

}
