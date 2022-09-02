package com.tosware.nkm.models.game.abilities.akame

import com.tosware.nkm.NkmConf
import com.tosware.nkm.models.game.Ability.{AbilityId, UseCheck}
import com.tosware.nkm.models.game.NkmCharacter.CharacterId
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.hex.HexUtils._

import scala.util.Random

object Eliminate {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Eliminate",
      abilityType = AbilityType.Normal,
      description = "Character deals critical damage dealing double damage to selected target in range.",
      variables = NkmConf.extract("abilities.akame.eliminate"),
    )
}

case class Eliminate(abilityId: AbilityId, parentCharacterId: CharacterId) extends Ability(abilityId) with UsableOnCharacter {
  override val metadata = Eliminate.metadata
  override val state = AbilityState(parentCharacterId)
  override def rangeCellCoords(implicit gameState: GameState) =
    parentCell.get.coordinates.getCircle(metadata.variables("range"))

  override def targetsInRange(implicit gameState: GameState) =
    rangeCellCoords.whereEnemiesOfC(parentCharacterId)

  override def use(target: CharacterId, useData: UseData)(implicit random: Random, gameState: GameState) = {
    gameState
      .abilityHitCharacter(id, target)
    ???
  }

  override def useChecks(implicit target: CharacterId, useData: UseData, gameState: GameState): Set[UseCheck] = {
    super.useChecks ++ Seq(
      UseCheck.TargetIsEnemy,
    )
  }
}
