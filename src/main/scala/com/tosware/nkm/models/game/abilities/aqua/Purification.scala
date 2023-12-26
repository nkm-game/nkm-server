package com.tosware.nkm.models.game.abilities.aqua

import com.tosware.nkm.*
import com.tosware.nkm.models.game.ability.*
import com.tosware.nkm.models.game.character.NkmCharacter
import com.tosware.nkm.models.game.character_effect.CharacterEffectType
import com.tosware.nkm.models.game.game_state.GameState
import com.tosware.nkm.models.game.hex.HexCoordinates

import scala.util.Random

object Purification extends NkmConf.AutoExtract {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Purification",
      abilityType = AbilityType.Normal,
      description =
        """Remove all negative effects from the target.
          |
          |Range: circular, {range}""".stripMargin,
      targetsMetadata = Seq(AbilityTargetMetadata.SingleCharacter),
    )
}

case class Purification(abilityId: AbilityId, parentCharacterId: CharacterId)
    extends Ability(abilityId) with Usable {
  override val metadata: AbilityMetadata = Purification.metadata
  override def rangeCellCoords(implicit gameState: GameState): Set[HexCoordinates] =
    defaultCircleRange(metadata.variables("range"))
  override def targetsInRange(implicit gameState: GameState): Set[HexCoordinates] =
    rangeCellCoords.whereFriendsOfC(parentCharacterId)
  override def use(useData: UseData)(implicit random: Random, gameState: GameState): GameState = {
    val target: CharacterId = useData.firstAsCharacterId
    val effectIdsToRemove = gameState.characterById(target).state.effects
      .filter(_.effectType == CharacterEffectType.Negative).map(_.id)

    gameState
      .abilityHitCharacter(id, target)
      .removeEffects(effectIdsToRemove)(random, id)
  }
  override def useChecks(implicit useData: UseData, gameState: GameState): Set[UseCheck] = {
    val target: CharacterId = useData.firstAsCharacterId
    val targetCharacter: NkmCharacter = gameState.characterById(target)
    super.useChecks ++ characterBaseUseChecks(target) ++ Seq(
      UseCheck.Character.IsFriend(target),
      targetCharacter.state.effects.exists(_.effectType == CharacterEffectType.Negative) ->
        "Target character does not have any negative effects.",
    )
  }
}
