package com.tosware.nkm.models.game.abilities.dekomori_sanae

import com.tosware.nkm.*
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.ability.*
import com.tosware.nkm.models.game.hex.HexCoordinates
import spray.json.*

import scala.util.Random

object MjolnirHammer extends NkmConf.AutoExtract {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Mjolnir Hammer",
      abilityType = AbilityType.Normal,
      description =
        """Character hits twice, dealing {damage} physical damage on each hit.
          |If both attacks target the same character, it will receive half damage from second hit.
          |
          |Range: circular, {range}""".stripMargin,

    )
}

case class MjolnirHammer(abilityId: AbilityId, parentCharacterId: CharacterId)
  extends Ability(abilityId, parentCharacterId)
    with Usable {
  override val metadata: AbilityMetadata = MjolnirHammer.metadata

  override def rangeCellCoords(implicit gameState: GameState): Set[HexCoordinates] =
    parentCell.get.coordinates.getCircle(metadata.variables("range")).whereExists

  override def targetsInRange(implicit gameState: GameState): Set[HexCoordinates] =
    rangeCellCoords.whereEnemiesOfC(parentCharacterId)

  override def use(useData: UseData)(implicit random: Random, gameState: GameState): GameState = {
    val baseDamageAmount = metadata.variables("damage")
    val baseDamage = Damage(DamageType.Physical, baseDamageAmount)
    val halfDamage = baseDamage.copy(amount = baseDamageAmount / 2)
    val targetCoords = useData.data.parseJson.convertTo[Seq[HexCoordinates]]
    val targetCharacterIds = targetCoords.flatMap(_.toCellOpt.flatMap(_.characterId))
    targetCharacterIds match {
      case Seq(singleCid) =>
        gameState
          .damageCharacter(singleCid, baseDamage)(random, id)
          .damageCharacter(singleCid, halfDamage)(random, id)
      case Seq(doubleCid1, doubleCid2) =>
        gameState
          .damageCharacter(doubleCid1, baseDamage)(random, id)
          .damageCharacter(doubleCid2, baseDamage)(random, id)
      case _ => gameState
    }
  }

  override def useChecks(implicit useData: UseData, gameState: GameState): Set[UseCheck] = {
    val targetCoords = useData.data.parseJson.convertTo[Seq[HexCoordinates]]
    super.useChecks ++ Seq(
      Seq(1, 2).contains(targetCoords.size) ->
        "You should provide 1 or 2 coordinates.",
      (targetCoords.size == targetCoords.toSet.size) ->
        "You should send a single coordinate only if you are trying to target one character.",
    ) ++
    targetCoords.flatMap(implicit coords => {
      Seq(
        UseCheck.TargetCoordinates.ExistsOnMap,
        UseCheck.TargetCoordinates.InRange,
      )
    }) ++
    targetCoords.toCells.flatMap(_.characterId).flatMap(implicit cid => {
      Seq(
        UseCheck.TargetCharacter.IsEnemy,
      )
    })
  }
}
