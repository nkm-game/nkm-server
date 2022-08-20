package com.tosware.NKM.models.game.abilities.sinon

import com.tosware.NKM.NKMConf
import com.tosware.NKM.models.{Damage, DamageType}
import com.tosware.NKM.models.game.Ability.{AbilityId, UseCheck}
import com.tosware.NKM.models.game.NKMCharacter.CharacterId
import com.tosware.NKM.models.game._
import com.tosware.NKM.models.game.abilities.sinon.PreciseShot.damage
import com.tosware.NKM.models.game.hex.HexUtils._

import scala.util.Random

object PreciseShot {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Precise Shot",
      abilityType = AbilityType.Ultimate,
      description = "Character shoots enemy dealing physical damage.",
      cooldown = NKMConf.int("abilities.sinon.preciseShot.cooldown"),
      range = NKMConf.int("abilities.sinon.preciseShot.range"),
    )
  val damage: Int = NKMConf.int("abilities.sinon.preciseShot.damage")
}

case class PreciseShot(abilityId: AbilityId, parentCharacterId: CharacterId) extends Ability(abilityId) with UsableOnCharacter {
  override val metadata = PreciseShot.metadata
  override val state = AbilityState(parentCharacterId)
  override def rangeCellCoords(implicit gameState: GameState) =
    parentCell.get.coordinates.getCircle(metadata.range).whereExists

  override def targetsInRange(implicit gameState: GameState) =
    rangeCellCoords.whereEnemiesOfC(parentCharacterId)

  override def use(target: CharacterId, useData: UseData)(implicit random: Random, gameState: GameState): GameState =
    gameState
      .abilityHitCharacter(id, target)
      .damageCharacter(target, Damage(DamageType.Physical, damage))(random, id)

  override def useChecks(implicit target: CharacterId, useData: UseData, gameState: GameState): Set[UseCheck] =
    super.useChecks + UseCheck.TargetIsEnemy
}
