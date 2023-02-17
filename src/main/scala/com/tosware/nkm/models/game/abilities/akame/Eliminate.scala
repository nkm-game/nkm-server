package com.tosware.nkm.models.game.abilities.akame

import com.tosware.nkm.NkmConf
import com.tosware.nkm.models.game.ability.Ability.{AbilityId, UseCheck}
import com.tosware.nkm.models.game.character.NkmCharacter.CharacterId
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.ability.{Ability, AbilityMetadata, AbilityType, UsableOnCharacter, UseData}

import scala.util.Random

object Eliminate {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Eliminate",
      abilityType = AbilityType.Normal,
      description =
        """Character hits critically, dealing double AD damage to target.
          |
          |Range: circular, {range}""".stripMargin,
      variables = NkmConf.extract("abilities.akame.eliminate"),
    )
}

case class Eliminate(abilityId: AbilityId, parentCharacterId: CharacterId) extends Ability(abilityId, parentCharacterId) with UsableOnCharacter {
  override val metadata = Eliminate.metadata

  override def rangeCellCoords(implicit gameState: GameState) =
    parentCell.get.coordinates.getCircle(metadata.variables("range"))

  override def targetsInRange(implicit gameState: GameState) =
    rangeCellCoords.whereEnemiesOfC(parentCharacterId)

  override def use(target: CharacterId, useData: UseData)(implicit random: Random, gameState: GameState) =
    gameState
      .abilityHitCharacter(id, target)
      .damageCharacter(target, Damage(DamageType.Physical, parentCharacter.state.attackPoints * 2))(random, id)

  override def useChecks(implicit target: CharacterId, useData: UseData, gameState: GameState): Set[UseCheck] = {
    super.useChecks ++ Seq(
      UseCheck.TargetCharacter.IsEnemy,
    )
  }
}
