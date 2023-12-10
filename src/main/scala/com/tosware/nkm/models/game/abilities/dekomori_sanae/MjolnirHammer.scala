package com.tosware.nkm.models.game.abilities.dekomori_sanae

import com.tosware.nkm.*
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.ability.*
import com.tosware.nkm.models.game.hex.HexCoordinates

import scala.util.Random

object MjolnirHammer extends NkmConf.AutoExtract {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Mjolnir Hammer",
      abilityType = AbilityType.Normal,
      description =
        """Hit twice, dealing {damage} physical damage on each hit.
          |If both attacks target the same character, second hit will deal half of the damage.
          |
          |Range: circular, {range}""".stripMargin,
      targetsMetadata = Seq(AbilityTargetMetadata(1 to 2, AbilityTargetType.Character)),
    )
}

case class MjolnirHammer(abilityId: AbilityId, parentCharacterId: CharacterId)
    extends Ability(abilityId)
    with Usable {
  override val metadata: AbilityMetadata = MjolnirHammer.metadata
  override def rangeCellCoords(implicit gameState: GameState): Set[HexCoordinates] =
    parentCell.get.coordinates.getCircle(metadata.variables("range")).whereExists
  override def targetsInRange(implicit gameState: GameState): Set[HexCoordinates] =
    rangeCellCoords.whereSeenEnemiesOfC(parentCharacterId)
  override def use(useData: UseData)(implicit random: Random, gameState: GameState): GameState = {
    val baseDamageAmount = metadata.variables("damage")
    val baseDamage = Damage(DamageType.Physical, baseDamageAmount)
    val halfDamage = baseDamage.copy(amount = baseDamageAmount / 2)
    val targetCharacterIds = useData.allAsCharacterIds.distinct
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
    val targetCharacterIds = useData.allAsCharacterIds.distinct
    val allowedRange = metadata.targetsMetadata.head.allowedRange
    super.useChecks
      ++ targetCharacterIds.flatMap(characterBaseUseChecks)
      ++ targetCharacterIds.map(UseCheck.Character.IsEnemy)
      ++ Seq(
        // TODO: check against targets metadata as a base check for all abilities
        allowedRange.contains(targetCharacterIds.size) ->
          s"You should provide between ${allowedRange.min} or ${allowedRange.max} characters."
      )
  }
}
