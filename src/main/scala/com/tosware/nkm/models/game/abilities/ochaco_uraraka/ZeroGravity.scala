package com.tosware.nkm.models.game.abilities.ochaco_uraraka

import com.tosware.nkm.*
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.abilities.ochaco_uraraka.ZeroGravity.applyZeroGravity
import com.tosware.nkm.models.game.ability.*
import com.tosware.nkm.models.game.hex.HexCoordinates

import scala.util.Random

object ZeroGravity extends NkmConf.AutoExtract {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Zero Gravity",
      alternateName = "無重力 (Zero Gurabiti)",
      abilityType = AbilityType.Passive,
      description =
        """Basic attacks apply Zero Gravity effect for {duration}t.
          |Characters with Zero Gravity effect can fly.""".stripMargin,
      relatedEffectIds = Seq(effects.Fly.metadata.id),
    )

  def applyZeroGravity(cid: CharacterId, gameState: GameState)(implicit random: Random, causedById: String): GameState =
    gameState.addEffect(cid, effects.Fly(randomUUID(), metadata.variables("duration"), effects.ZeroGravity.metadata))
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
  override def basicAttack(targetCharacterId: CharacterId)(implicit random: Random, gameState: GameState): GameState = {
    val zeroGravityAppliedGs = applyZeroGravity(targetCharacterId, gameState)(random, id)

    if (gameState.characterById(targetCharacterId).isFriendForC(parentCharacterId))
      zeroGravityAppliedGs
    else
      parentCharacter.defaultBasicAttack(targetCharacterId)(random, zeroGravityAppliedGs)
  }
}
