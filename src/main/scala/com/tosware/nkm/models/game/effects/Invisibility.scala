package com.tosware.nkm.models.game.effects

import com.tosware.nkm.*
import com.tosware.nkm.models.game.ability.AbilityTrait
import com.tosware.nkm.models.game.character_effect.*
import com.tosware.nkm.models.game.event.GameEvent
import com.tosware.nkm.models.game.game_state.GameState

import scala.util.Random

object Invisibility {
  val metadata: CharacterEffectMetadata =
    CharacterEffectMetadata(
      name = CharacterEffectName.Invisibility,
      initialEffectType = CharacterEffectType.Positive,
      description = "State and position on the map are hidden.",
    )
}

case class Invisibility(effectId: CharacterEffectId, initialCooldown: Int)
    extends CharacterEffect(effectId) {
  val metadata: CharacterEffectMetadata = Invisibility.metadata

  override def onEventReceived(e: GameEvent.GameEvent)(implicit random: Random, gameState: GameState): GameState =
    e match {
      case GameEvent.CharacterBasicAttacked(_, characterId, targetCharacterId) =>
        val parentAttackedOther = characterId == parentCharacter.id && parentCharacter.isEnemyForC(targetCharacterId)
        val otherAttackedParent = targetCharacterId == parentCharacter.id && parentCharacter.isEnemyForC(characterId)

        if (!(parentAttackedOther || otherAttackedParent)) return gameState

        gameState.removeEffect(id)(random, id)
      case GameEvent.AbilityUseFinished(_, abilityId) =>
        val ability = gameState.abilityById(abilityId)
        if (ability.parentCharacter.id != parentCharacter.id) return gameState
        if (!ability.metadata.traits.contains(AbilityTrait.ContactEnemy)) return gameState

        gameState.removeEffect(id)(random, id)
      case GameEvent.MovementInterrupted(context, _) =>
        if (context.causedById != parentCharacter.id) return gameState

        gameState.removeEffect(id)(random, id)
      case _ => gameState
    }

  override def description(implicit gameState: GameState): String =
    metadata.description
}
