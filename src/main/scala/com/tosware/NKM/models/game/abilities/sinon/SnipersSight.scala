package com.tosware.NKM.models.game.abilities.sinon

import com.tosware.NKM.models.game.Ability.AbilityId
import com.tosware.NKM.models.game.NKMCharacter.CharacterId
import com.tosware.NKM.models.game._
import com.tosware.NKM.models.game.hex.HexCoordinates
import com.tosware.NKM.models.game.hex.HexUtils._

import scala.util.Random

object SnipersSight {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Sniper's Sight",
      abilityType = AbilityType.Passive,
      description = "Basic attack range of this character is round.",
    )
}

case class SnipersSight
(
  abilityId: AbilityId,
  parentCharacterId: CharacterId,
) extends Ability(abilityId) with BasicAttackOverride {
  override val metadata = SnipersSight.metadata
  override val state = AbilityState(parentCharacterId)

  override def basicAttackCells(implicit gameState: GameState): Set[HexCoordinates] = {
    if(parentCell.isEmpty) return Set.empty
    val parentCoordinates = parentCell.get.coordinates
    parentCharacter.state.attackType match {
      case AttackType.Melee => parentCoordinates.getCircle(parentCharacter.state.basicAttackRange).whereExists // TODO: stop at walls and characters
      case AttackType.Ranged => parentCoordinates.getCircle(parentCharacter.state.basicAttackRange).whereExists
    }
  }

  override def basicAttackTargets(implicit gameState: GameState): Set[HexCoordinates] =
    parentCharacter.defaultBasicAttackTargets

  override def basicAttack(targetCharacterId: CharacterId)(implicit random: Random, gameState: GameState): GameState =
    parentCharacter.defaultBasicAttack(targetCharacterId)
}
