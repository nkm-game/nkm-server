package com.tosware.nkm.models.game.effects

import com.tosware.nkm.*
import com.tosware.nkm.models.game.GameState
import com.tosware.nkm.models.game.ability.AbilityTrait
import com.tosware.nkm.models.game.character_effect.*
import com.tosware.nkm.models.game.event.{GameEvent, GameEventListener}

import scala.util.Random

object Invisibility {
  val metadata: CharacterEffectMetadata =
    CharacterEffectMetadata(
      name = CharacterEffectName.Invisibility,
      initialEffectType = CharacterEffectType.Positive,
      description =
        """State and position on the map are hidden.
          |Accidentally walking into this character breaks invisibility.""".stripMargin,
    )
}

case class Invisibility(effectId: CharacterEffectId, initialCooldown: Int)
    extends CharacterEffect(effectId)
    with GameEventListener {
  val metadata: CharacterEffectMetadata = Invisibility.metadata

  override def onEvent(e: GameEvent.GameEvent)(implicit random: Random, gameState: GameState): GameState =
    e match {
      case GameEvent.CharacterBasicAttacked(_, _, _, _, characterId, targetCharacterId) =>
        if (characterId != parentCharacter.id) return gameState
        if (parentCharacter.isFriendForC(targetCharacterId)) return gameState

        gameState.removeEffect(id)(random, id)
      case GameEvent.AbilityUseFinished(_, _, _, _, abilityId) =>
        val ability = gameState.abilityById(abilityId)
        if (ability.parentCharacter.id != parentCharacter.id) return gameState
        if (!ability.metadata.traits.contains(AbilityTrait.ContactEnemy)) return gameState

        gameState.removeEffect(id)(random, id)
      case _ => gameState
    }
}
