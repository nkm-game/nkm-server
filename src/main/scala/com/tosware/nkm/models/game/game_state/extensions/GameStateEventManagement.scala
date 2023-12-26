package com.tosware.nkm.models.game.game_state.extensions

import com.softwaremill.quicklens.*
import com.tosware.nkm.*
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.event.*
import com.tosware.nkm.models.game.event.GameEvent.*
import com.tosware.nkm.models.game.game_state.GameState

import scala.util.Random

object GameStateEventManagement extends GameStateEventManagement
trait GameStateEventManagement {
  implicit class GameStateEventManagement(gs: GameState) {
    def handleTrigger(event: GameEvent, trigger: GameEventListener)(
        implicit
        random: Random,
        gameState: GameState,
    ): GameState =
      try
        trigger.onEvent(event)(random, gameState)
      catch {
        case e: Throwable =>
          gs.logger.error(e.getMessage)
          gameState
      }

    def executeEventTriggers(e: GameEvent)(implicit random: Random): GameState = {
      val stateAfterAbilityTriggers = gs.triggerAbilities.foldLeft(gs) { (acc, ability) =>
        handleTrigger(e, ability)(random, acc)
      }
      gs.triggerEffects.foldLeft(stateAfterAbilityTriggers) { (acc, effect) =>
        handleTrigger(e, effect)(random, acc)
      }
    }

    def _logEventWithoutChecks(e: GameEvent)(implicit random: Random) =
      gs.copy(gameLog = gs.gameLog.modify(_.events).using(es => es :+ e))
        .executeEventTriggers(e)

    def logEvent(e: GameEvent)(implicit random: Random): GameState = {
      val targetCharacter = e match {
        case EffectAddedToCharacter(_, _, _, _, _, _, characterId) =>
          gs.characterById(characterId)
        case EffectRemovedFromCharacter(_, _, _, _, _, _, characterId) =>
          gs.characterById(characterId)
        case EffectVariableSet(_, _, _, _, effectId, _, _) =>
          gs.effectById(effectId).parentCharacter(gs)
        case AbilityHitCharacter(_, _, _, _, _, targetCharacterId) =>
          gs.characterById(targetCharacterId)
        case AbilityUsed(_, _, _, _, abilityId) =>
          gs.abilityById(abilityId).parentCharacter(gs)
        case AbilityUseFinished(_, _, _, _, abilityId) =>
          gs.abilityById(abilityId).parentCharacter(gs)
        case AbilityVariableSet(_, _, _, _, abilityId, _, _) =>
          gs.abilityById(abilityId).parentCharacter(gs)
        case CharacterBasicMoved(_, _, _, _, characterId, _) =>
          gs.characterById(characterId)
        case CharacterPreparedToAttack(_, _, _, _, characterId, _) =>
          gs.characterById(characterId)
        case CharacterBasicAttacked(_, _, _, _, characterId, _) =>
          gs.characterById(characterId)
        case CharacterTeleported(_, _, _, _, characterId, _) =>
          gs.characterById(characterId)
        case ShieldDamaged(_, _, _, _, characterId, _) =>
          gs.characterById(characterId)
        case CharacterDamaged(_, _, _, _, characterId, _) =>
          gs.characterById(characterId)
        case CharacterHealed(_, _, _, _, characterId, _) =>
          gs.characterById(characterId)
        case CharacterHpSet(_, _, _, _, characterId, _) =>
          gs.characterById(characterId)
        case CharacterShieldSet(_, _, _, _, characterId, _) =>
          gs.characterById(characterId)
        case CharacterStatSet(_, _, _, _, characterId, _, _) =>
          gs.characterById(characterId)
        case CharacterRemovedFromMap(_, _, _, _, characterId) =>
          gs.characterById(characterId)
        case BasicAttackRefreshed(_, _, _, _, characterId) =>
          gs.characterById(characterId)
        case BasicMoveRefreshed(_, _, _, _, characterId) =>
          gs.characterById(characterId)
        case _ =>
          null
      }

      if (targetCharacter != null && targetCharacter.isInvisible) {
        logAndHideEvent(
          e,
          Seq(targetCharacter.owner(gs).id),
          RevealCondition.RelatedCharacterRevealed(targetCharacter.id),
        )
      } else {
        _logEventWithoutChecks(e)
      }
    }

    def logEvents(es: Seq[GameEvent])(implicit random: Random): GameState =
      es.foldLeft(gs) { case (acc, event) => acc.logEvent(event) }

    def logAndHideEvent(e: GameEvent, showOnlyFor: Seq[PlayerId], revealCondition: RevealCondition)(
        implicit random: Random
    ): GameState =
      gs.copy(hiddenEvents = gs.hiddenEvents :+ event.EventHideData(e.id, showOnlyFor, revealCondition))
        ._logEventWithoutChecks(e)

    def reveal(revealCondition: RevealCondition)(implicit random: Random): GameState =
      gs.copy(hiddenEvents = gs.hiddenEvents.filterNot(_.revealCondition == revealCondition))
        ._logEventWithoutChecks(EventsRevealed(
          randomUUID(),
          gs.phase,
          gs.turn,
          gs.id,
          gs.hiddenEvents.filter(_.revealCondition == revealCondition).map(_.eid),
        ))

  }
}
