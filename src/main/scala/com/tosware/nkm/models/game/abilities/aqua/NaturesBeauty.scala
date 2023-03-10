package com.tosware.nkm.models.game.abilities.aqua

import com.tosware.nkm._
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.ability._
import com.tosware.nkm.models.game.hex.HexCoordinates

import scala.util.Random

object NaturesBeauty {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Nature's Beauty",
      abilityType = AbilityType.Passive,
      description = "Character can use basic attacks on allies, healing them instead of dealing damage.",
    )
}

case class NaturesBeauty(abilityId: AbilityId, parentCharacterId: CharacterId)
  extends Ability(abilityId, parentCharacterId)
    with BasicAttackOverride {
  override val metadata: AbilityMetadata = NaturesBeauty.metadata

  override def rangeCellCoords(implicit gameState: GameState): Set[HexCoordinates] =
    gameState.characterById(parentCharacterId).basicAttackCellCoords(gameState)
  override def targetsInRange(implicit gameState: GameState): Set[HexCoordinates] =
    rangeCellCoords.whereFriendsOfC(parentCharacterId)
  override def basicAttackCells(implicit gameState: GameState): Set[HexCoordinates] =
    parentCharacter.defaultBasicAttackCells
  override def basicAttackTargets(implicit gameState: GameState): Set[HexCoordinates] =
    basicAttackCells.whereCharacters
  override def basicAttack(targetCharacterId: CharacterId)(implicit random: Random, gameState: GameState): GameState =
    if(gameState.characterById(targetCharacterId).isFriendForC(parentCharacterId))
      gameState
        .heal(targetCharacterId, parentCharacter.state.attackPoints)(random, id)
    else
      parentCharacter
        .defaultBasicAttack(targetCharacterId)

}
