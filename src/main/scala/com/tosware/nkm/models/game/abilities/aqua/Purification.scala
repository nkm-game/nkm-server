package com.tosware.nkm.models.game.abilities.aqua

import com.tosware.nkm._
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.ability._
import com.tosware.nkm.models.game.character.NkmCharacter
import com.tosware.nkm.models.game.character_effect.CharacterEffectType

import scala.util.Random

object Purification {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Purification",
      abilityType = AbilityType.Normal,
      description =
        """Character removes all negative effects from target.
          |
          |Range: circular, {range}""".stripMargin,
      variables = NkmConf.extract("abilities.aqua.purification"),
    )
}

case class Purification(abilityId: AbilityId, parentCharacterId: CharacterId) extends Ability(abilityId, parentCharacterId) with UsableOnCharacter {
  override val metadata = Purification.metadata

  override def rangeCellCoords(implicit gameState: GameState) =
    parentCell.get.coordinates.getCircle(metadata.variables("range")).whereExists

  override def targetsInRange(implicit gameState: GameState) =
    rangeCellCoords.whereFriendsOfC(parentCharacterId)

  override def use(target: CharacterId, useData: UseData)(implicit random: Random, gameState: GameState) = {
    val effectIdsToRemove = gameState.characterById(target).state.effects
      .filter(_.effectType == CharacterEffectType.Negative).map(_.id)

    gameState
      .abilityHitCharacter(id, target)
      .removeEffects(effectIdsToRemove)(random, id)
  }

  override def useChecks(implicit target: CharacterId, useData: UseData, gameState: GameState): Set[UseCheck] = {
    val targetCharacter: NkmCharacter = gameState.characterById(target)

    super.useChecks ++ Seq(
      UseCheck.TargetCharacter.IsFriend,
      targetCharacter.state.effects.exists(_.effectType == CharacterEffectType.Negative) ->
        "Target character does not have any negative effects.",
    )
  }
}
