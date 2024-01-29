package com.tosware.nkm.models.game.game_state.extensions

import com.tosware.nkm.*
import com.tosware.nkm.models.game.event.GameEvent.*
import com.tosware.nkm.models.game.game_state.GameState
import spray.json.*

import scala.util.Random
import com.tosware.nkm.serializers.NkmJsonProtocol.*

object GameStateAbilityUtils extends GameStateAbilityUtils
trait GameStateAbilityUtils {
  implicit class GameStateAbilityUtils(gs: GameState) {
    def abilityHitCharacter(abilityId: AbilityId, targetCharacter: CharacterId)(implicit random: Random): GameState = {
      implicit val causedById: String = abilityId
      gs.logEvent(AbilityHitCharacter(gs.generateEventContext(), abilityId, targetCharacter))
    }

    def setAbilityEnabled(abilityId: AbilityId, newEnabled: Boolean): GameState = {
      val newState = gs.abilityById(abilityId).getEnabledChangedState(newEnabled)(gs)
      gs.copy(abilityStates = gs.abilityStates.updated(abilityId, newState))
    }
    private val canBeDisabledKey = "canBeDisabled"

    def setAbilityCanBeDisabled(abilityId: AbilityId, flag: Boolean)(implicit random: Random): GameState =
      gs.setAbilityVariable(abilityId, canBeDisabledKey, flag.toJson.toString)

    def abilityCanBeDisabled(abilityId: AbilityId): Boolean =
      gs.abilityStates(abilityId).variables.get(canBeDisabledKey).fold(false)(_.parseJson.convertTo[Boolean])

    def setAbilityVariable(abilityId: AbilityId, key: String, value: String)(implicit random: Random): GameState = {
      implicit val causedById: String = abilityId
      val newState = gs.abilityById(abilityId).getVariablesChangedState(key, value)(gs)
      gs.copy(abilityStates = gs.abilityStates.updated(abilityId, newState))
        .logEvent(AbilityVariableSet(gs.generateEventContext(), abilityId, key, value))
    }
  }
}
