package com.tosware.nkm.models.game.abilities.blank

import com.tosware.nkm.NkmConf
import com.tosware.nkm.models.game.Ability.{AbilityId, UseCheck}
import com.tosware.nkm.models.game.NkmCharacter.CharacterId
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.hex.HexUtils._

import scala.util.Random

object Check {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Check",
      abilityType = AbilityType.Normal,
      description =
        """Character forces selected enemy character to take action.
          |It cannot use a basic attack this turn.""".stripMargin,
      variables = NkmConf.extract("abilities.blank.check"),
    )
}

case class Check(abilityId: AbilityId, parentCharacterId: CharacterId) extends Ability(abilityId) with UsableOnCharacter {
  override val metadata = Check.metadata
  override val state = AbilityState(parentCharacterId)
  override def rangeCellCoords(implicit gameState: GameState) =
    gameState.hexMap.get.cells.toCoords

  // TODO: enemy did not take action in phase before
  override def targetsInRange(implicit gameState: GameState) = {
    rangeCellCoords.whereEnemiesOfC(parentCharacterId)
    ???
  }

  override def use(target: CharacterId, useData: UseData)(implicit random: Random, gameState: GameState) = {
    gameState
      .abilityHitCharacter(id, target)
    ???
  }

  override def useChecks(implicit target: CharacterId, useData: UseData, gameState: GameState): Set[UseCheck] = {
    val targetCharacter: NkmCharacter = gameState.characterById(target).get

    // TODO: enemy did not take action in phase before
    ???

    super.useChecks ++ Seq(
      UseCheck.TargetIsEnemy,
    )
  }
}
