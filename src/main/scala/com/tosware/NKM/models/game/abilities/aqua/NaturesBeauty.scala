package com.tosware.NKM.models.game.abilities.aqua

import com.tosware.NKM.models.game.NKMCharacter.CharacterId
import com.tosware.NKM.models.game._
import com.tosware.NKM.models.game.hex.HexUtils._

object NaturesBeauty {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Nature's Beauty",
      abilityType = AbilityType.Passive,
      description = "Character can use basic attacks on allies, healing them instead of dealing damage.",
    )
}

case class NaturesBeauty(parentCharacterId: CharacterId) extends Ability with BasicAttackOverride {
  override val metadata = NaturesBeauty.metadata
  override val state = AbilityState(parentCharacterId)
  override def rangeCellCoords(implicit gameState: GameState) =
    gameState.characterById(state.parentCharacterId).get.basicAttackCellCoords(gameState)
  override def targetsInRange(implicit gameState: GameState) =
    rangeCellCoords.whereFriendsOf(parentCharacterId)
  override def basicAttackCells(implicit gameState: GameState) = parentCharacter.defaultBasicAttackCells
  override def basicAttackTargets(implicit gameState: GameState) = parentCharacter.basicAttackTargets
  override def basicAttack(targetCharacterId: CharacterId)(implicit gameState: GameState) =
    if(gameState.characterById(targetCharacterId).get.isFriendFor(parentCharacterId)) {
      gameState.heal(targetCharacterId, parentCharacter.state.attackPoints)(parentCharacterId)
    } else parentCharacter.defaultBasicAttack(targetCharacterId)

}
