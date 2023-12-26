package com.tosware.nkm.models.game.abilities.akame

import com.tosware.nkm.*
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.ability.*
import com.tosware.nkm.models.game.character_effect.CharacterEffectName
import com.tosware.nkm.models.game.effects.MurasamePoison
import com.tosware.nkm.models.game.event.GameEvent.*
import com.tosware.nkm.models.game.event.{GameEvent, GameEventListener}
import com.tosware.nkm.models.game.game_state.GameState

import scala.util.Random

object Murasame extends NkmConf.AutoExtract {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Murasame",
      abilityType = AbilityType.Passive,
      description =
        """Apply Poison on basic attack or ability hit.
          |This Poison stacks and is permanent.
          |Deal {poisonDamage} true damage per stack at the end of the target's turn.
          |After applying {poisonStacksToDie} stacks, the target dies immediately.""".stripMargin,
      relatedEffectIds = Seq(MurasamePoison.metadata.id),
    )
}

case class Murasame(abilityId: AbilityId, parentCharacterId: CharacterId) extends Ability(abilityId)
    with GameEventListener {
  override val metadata: AbilityMetadata = Murasame.metadata

  private def killCharacterIfFullyStacked(targetCharacterId: CharacterId)(implicit
      random: Random,
      gameState: GameState,
  ): GameState = {
    val numberOfStacks = gameState
      .characterById(targetCharacterId)
      .state
      .effects
      .count(e => e.metadata.name == CharacterEffectName.MurasamePoison)

    if (numberOfStacks >= metadata.variables("poisonStacksToDie")) {
      gameState.executeCharacter(targetCharacterId)(random, id)
    } else gameState
  }

  private def applyPoison(targetCharacterId: CharacterId)(implicit random: Random, gameState: GameState): GameState = {
    val poisonEffect = effects.Poison(
      randomUUID(),
      Int.MaxValue,
      Damage(DamageType.True, metadata.variables("poisonDamage")),
      MurasamePoison.metadata,
    )
    gameState.addEffect(targetCharacterId, poisonEffect)(random, id)
  }

  override def onEvent(e: GameEvent.GameEvent)(implicit random: Random, gameState: GameState): GameState =
    e match {
      case CharacterBasicAttacked(_, _, _, _, characterId, targetCharacterId) =>
        if (parentCharacterId == characterId) {
          applyPoison(targetCharacterId)
        } else gameState
      case AbilityHitCharacter(_, _, _, _, abilityId, targetCharacterId) =>
        if (parentCharacterId == gameState.abilityById(abilityId).parentCharacter.id) {
          applyPoison(targetCharacterId)
        } else gameState
      case EffectAddedToCharacter(_, _, _, _, _, _, targetCharacterId) =>
        killCharacterIfFullyStacked(targetCharacterId)
      case _ => gameState
    }
}
