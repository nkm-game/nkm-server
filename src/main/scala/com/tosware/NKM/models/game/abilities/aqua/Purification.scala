package com.tosware.NKM.models.game.abilities.aqua

import com.tosware.NKM.NKMConf
import com.tosware.NKM.models.game.Ability.{AbilityId, UseCheck}
import com.tosware.NKM.models.game.NKMCharacter.CharacterId
import com.tosware.NKM.models.game._
import com.tosware.NKM.models.game.hex.HexUtils._

import scala.util.Random

object Purification {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Purification",
      abilityType = AbilityType.Normal,
      description = "Character removes all negative effects from target.",
      cooldown = NKMConf.int("abilities.aqua.purification.cooldown"),
      range = NKMConf.int("abilities.aqua.purification.range"),
    )
}

case class Purification(abilityId: AbilityId, parentCharacterId: CharacterId) extends Ability(abilityId) with UsableOnCharacter {
  override val metadata = Purification.metadata
  override val state = AbilityState(parentCharacterId)
  override def rangeCellCoords(implicit gameState: GameState) =
    parentCell.get.coordinates.getCircle(metadata.range).whereExists

  override def targetsInRange(implicit gameState: GameState) =
    rangeCellCoords.whereFriendsOfC(parentCharacterId)

  override def use(target: CharacterId, useData: UseData)(implicit random: Random, gameState: GameState) = {
    val effectIdsToRemove = gameState.characterById(target).get.state.effects
      .filter(_.metadata.effectType == CharacterEffectType.Negative).map(_.id)

    gameState
      .abilityHitCharacter(id, target)
      .removeEffects(effectIdsToRemove)(random, id)
  }

  override def useChecks(implicit target: CharacterId, useData: UseData, gameState: GameState): Set[UseCheck] = {
    val targetCharacter: NKMCharacter = gameState.characterById(target).get

    super.useChecks ++ Seq(
      UseCheck.TargetIsFriend,
      targetCharacter.state.effects.exists(_.metadata.effectType == CharacterEffectType.Negative) ->
        "Target character does not have any negative effects.",
    )
  }
}
