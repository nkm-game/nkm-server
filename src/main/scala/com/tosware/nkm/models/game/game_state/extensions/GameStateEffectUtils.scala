package com.tosware.nkm.models.game.game_state.extensions

import com.softwaremill.quicklens.*
import com.tosware.nkm.*
import com.tosware.nkm.models.game.character_effect.*
import com.tosware.nkm.models.game.event.GameEvent.*
import com.tosware.nkm.models.game.game_state.GameState

import scala.util.Random

object GameStateEffectUtils extends GameStateEffectUtils
trait GameStateEffectUtils {
  implicit class GameStateEffectUtils(gs: GameState) {
    def addEffect(characterId: CharacterId, characterEffect: CharacterEffect)(
        implicit
        random: Random,
        causedById: String,
    ): GameState = {
      val character = gs.characterById(characterId)
      val wasCharacterInvisible = character.isInvisible

      gs.updateCharacter(characterId)(_.addEffect(characterEffect))
        .modify(_.characterEffectStates)
        .using(_.updated(
          characterEffect.id,
          CharacterEffectState(
            characterEffect.metadata.name,
            characterEffect.initialCooldown,
          ),
        ))
        .logEvent(EffectAddedToCharacter(
          gs.generateEventContext(),
          characterEffect.metadata.id,
          characterEffect.id,
          characterId,
        ))
        .checkIfCharacterWentInvisible(characterId, wasCharacterInvisible)
    }

    def removeEffects(characterEffectIds: Seq[CharacterEffectId])(
        implicit
        random: Random,
        causedById: String,
    ): GameState =
      characterEffectIds.foldLeft(gs) { case (acc, eid) => acc.removeEffect(eid) }

    def removeEffect(characterEffectId: CharacterEffectId)(implicit random: Random, causedById: String): GameState = {
      val effect = gs.effectById(characterEffectId)
      val character = effect.parentCharacter(gs)
      val wasCharacterInvisible = character.isInvisible

      gs.updateCharacter(character.id)(_.removeEffect(characterEffectId))
        .modify(_.characterEffectStates).using(ces => ces.removed(characterEffectId))
        .logEvent(EffectRemovedFromCharacter(
          gs.generateEventContext(),
          effect.metadata.id,
          characterEffectId,
          character.id,
        ))
        .checkIfCharacterRevealed(character.id, wasCharacterInvisible)
    }

    def setEffectVariable(effectId: CharacterEffectId, key: String, value: String)(implicit
        random: Random
    ): GameState = {
      implicit val causedById: String = effectId
      val newState = gs.effectById(effectId).getVariablesChangedState(key, value)(gs)
      gs.copy(characterEffectStates = gs.characterEffectStates.updated(effectId, newState))
        .logEvent(EffectVariableSet(gs.generateEventContext(), effectId, key, value))
    }

  }
}
