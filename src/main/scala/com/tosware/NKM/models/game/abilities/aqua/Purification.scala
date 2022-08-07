package com.tosware.NKM.models.game.abilities.aqua

import com.softwaremill.quicklens._
import com.tosware.NKM.models.game.Ability.UseCheck
import com.tosware.NKM.models.game.NKMCharacter.CharacterId
import com.tosware.NKM.models.game._
import com.tosware.NKM.models.game.hex.HexUtils._

object Purification {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Purification",
      abilityType = AbilityType.Normal,
      description = "*Character* removes all negative effects from target.",
      cooldown = 4,
      range = 4,
    )
}

case class Purification(parentCharacterId: CharacterId) extends Ability with UsableOnCharacter {
  override val metadata = Purification.metadata
  override val state = AbilityState(parentCharacterId)
  override def rangeCellCoords(implicit gameState: GameState) =
    parentCell.get.coordinates.getCircle(metadata.range).whereExists

  override def targetsInRange(implicit gameState: GameState) =
    rangeCellCoords.whereFriendsOf(parentCharacterId)

  override def use(target: CharacterId, useData: UseData)(implicit gameState: GameState) =
    gameState.updateCharacter(target)(
      _.modify(_.state.effects)
        .using(_.filterNot(_.metadata.effectType == CharacterEffectType.Negative))
    )

  override def useChecks(implicit target: CharacterId, useData: UseData, gameState: GameState): Set[UseCheck] = {
    val targetCharacter: NKMCharacter = gameState.characterById(target).get

    super.useChecks ++ Seq(
      UseCheck.TargetIsFriend,
      targetCharacter.state.effects.exists(_.metadata.effectType == CharacterEffectType.Negative) ->
        "Target character does not have any negative effects.",
    )
  }
}
