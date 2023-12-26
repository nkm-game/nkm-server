package com.tosware.nkm.models.game.game_state.extensions

import com.tosware.nkm.*
import com.tosware.nkm.models.game.event.GameEvent.*
import com.tosware.nkm.models.game.game_state.GameState

import scala.util.Random

object GameStateAbilityUtils extends GameStateAbilityUtils
trait GameStateAbilityUtils {
  implicit class GameStateAbilityUtils(gs: GameState) {
    def abilityHitCharacter(abilityId: AbilityId, targetCharacter: CharacterId)(implicit random: Random): GameState = {
      implicit val causedById: String = abilityId
      gs.logEvent(AbilityHitCharacter(
        randomUUID(),
        gs.phase,
        gs.turn,
        causedById,
        abilityId,
        targetCharacter,
      ))
    }

    def setAbilityEnabled(abilityId: AbilityId, newEnabled: Boolean): GameState = {
      val newState = gs.abilityById(abilityId).getEnabledChangedState(newEnabled)(gs)
      gs.copy(abilityStates = gs.abilityStates.updated(abilityId, newState))
    }

    def setAbilityVariable(abilityId: AbilityId, key: String, value: String)(implicit random: Random): GameState = {
      implicit val causedById: String = abilityId
      val newState = gs.abilityById(abilityId).getVariablesChangedState(key, value)(gs)
      gs.copy(abilityStates = gs.abilityStates.updated(abilityId, newState))
        .logEvent(AbilityVariableSet(randomUUID(), gs.phase, gs.turn, causedById, abilityId, key, value))
    }
  }
}
