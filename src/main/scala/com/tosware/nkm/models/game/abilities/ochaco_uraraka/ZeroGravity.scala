package com.tosware.nkm.models.game.abilities.ochaco_uraraka

import com.tosware.nkm._
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.ability._
import com.tosware.nkm.models.game.hex.HexCoordinates

import scala.util.Random

object ZeroGravity {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Zero Gravity",
      alternateName = "無重力 (Zero Gurabiti)",
      abilityType = AbilityType.Passive,
      description =
        """Character can attack friendly characters, but instead of dealing damage applies Zero Gravity effect on them for {duration}t.
          |Characters with Zero Gravity effect can fly.""".stripMargin,
      variables = NkmConf.extract("abilities.ochaco_uraraka.zeroGravity"),
    )
}

case class ZeroGravity(abilityId: AbilityId, parentCharacterId: CharacterId)
  extends Ability(abilityId, parentCharacterId)
    with BasicAttackOverride {
  override val metadata: AbilityMetadata = ZeroGravity.metadata

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
    else
      parentCharacter
        .defaultBasicAttack(targetCharacterId)

}
