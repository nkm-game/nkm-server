package com.tosware.nkm.models.game.abilities.aqua

import com.tosware.nkm.*
import com.tosware.nkm.models.game.ability.*
import com.tosware.nkm.models.game.game_state.GameState
import com.tosware.nkm.models.game.hex.HexCoordinates

import scala.util.Random

object NaturesBeauty extends NkmConf.AutoExtract {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Nature's Beauty",
      abilityType = AbilityType.Passive,
      description = "Basic attacks can be used on allies, healing them instead of dealing damage.",
      traits = Seq(AbilityTrait.ContactFriend),
    )
}

case class NaturesBeauty(abilityId: AbilityId, parentCharacterId: CharacterId)
    extends Ability(abilityId)
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
    if (gameState.characterById(targetCharacterId).isFriendForC(parentCharacterId))
      gameState
        .heal(targetCharacterId, parentCharacter.state.attackPoints)(random, id)
    else
      parentCharacter
        .defaultBasicAttack(targetCharacterId)

}
