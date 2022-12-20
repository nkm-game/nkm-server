package com.tosware.nkm.models.game.abilities.aqua

import com.tosware.nkm.models.game.Ability.AbilityId
import com.tosware.nkm.models.game.NkmCharacter.CharacterId
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.hex.HexUtils._

import scala.util.Random

object NaturesBeauty {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Nature's Beauty",
      abilityType = AbilityType.Passive,
      description = "Character can use basic attacks on allies, healing them instead of dealing damage.",
    )
}

case class NaturesBeauty(abilityId: AbilityId, parentCharacterId: CharacterId) extends Ability(abilityId, parentCharacterId) with BasicAttackOverride {
  override val metadata = NaturesBeauty.metadata

  override def rangeCellCoords(implicit gameState: GameState) =
    gameState.characterById(parentCharacterId).get.basicAttackCellCoords(gameState)
  override def targetsInRange(implicit gameState: GameState) =
    rangeCellCoords.whereFriendsOfC(parentCharacterId)
  override def basicAttackCells(implicit gameState: GameState) = parentCharacter.defaultBasicAttackCells
  override def basicAttackTargets(implicit gameState: GameState) = basicAttackCells.whereCharacters
  override def basicAttack(targetCharacterId: CharacterId)(implicit random: Random, gameState: GameState) =
    if(gameState.characterById(targetCharacterId).get.isFriendForC(parentCharacterId)) {
      gameState.heal(targetCharacterId, parentCharacter.state.attackPoints)(random, parentCharacterId)
    } else parentCharacter.defaultBasicAttack(targetCharacterId)

}
