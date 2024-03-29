package com.tosware.nkm.models.game.game_state.extensions

import com.softwaremill.quicklens.*
import com.tosware.nkm.*
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.character.NkmCharacter
import com.tosware.nkm.models.game.effects.*
import com.tosware.nkm.models.game.event.*
import com.tosware.nkm.models.game.event.GameEvent.*
import com.tosware.nkm.models.game.game_state.{GameState, GameStatus}
import com.tosware.nkm.models.game.hex.*

import scala.util.Random

object GameStateInternalTriggers extends GameStateInternalTriggers
trait GameStateInternalTriggers {
  implicit class GameStateInternalTriggers(gs: GameState) {
    def decrementEffectCooldown(effectId: CharacterEffectId)(implicit random: Random): GameState = {
      val newState = gs.effectById(effectId).getDecrementCooldownState(gs)
      if (newState.cooldown > 0) {
        gs.copy(characterEffectStates = gs.characterEffectStates.updated(effectId, newState))
      } else {
        gs.removeEffect(effectId)(random, gs.id)
      }
    }

    def incrementTurn(): GameState =
      gs.modify(_.turn).using(oldTurn => Turn(oldTurn.number + 1))

    def startTurn()(implicit random: Random, causedById: String = gs.id): GameState =
      gs.logEvent(TurnStarted(gs.generateEventContext(), gs.currentPlayer.id))
        .increaseTime(gs.currentPlayer.id, gs.clockConfig.incrementMillis)

    def skipTurnIfNoCharactersToTakeAction(): GameState =
      if (gs.currentPlayer.characterIds.intersect(gs.charactersToTakeAction).isEmpty)
        incrementTurn()
          .skipTurnIfNoCharactersToTakeAction()
      else gs

    def skipTurnIfPlayerKnockedOut(): GameState = {
      if (gs.gameStatus != GameStatus.Running) return gs // prevent infinite loop if no one is playing
      if (gs.currentPlayer.victoryStatus != VictoryStatus.Pending)
        incrementTurn()
      else gs
    }

    def decrementEndTurnCooldowns()(implicit random: Random): GameState = {
      val currentCharacter: NkmCharacter = gs.currentCharacterOpt.getOrElse(return gs)
      val currentCharacterAbilityIds = currentCharacter.state.abilities.map(_.id)
      val currentCharacterEffectIds = currentCharacter.state.effects.map(_.id)

      val decrementAbilityCooldownsState = currentCharacterAbilityIds.foldLeft(gs) { (acc, abilityId) =>
        acc.decrementAbilityCooldown(abilityId)
      }

      val decrementEffectCooldownsState = currentCharacterEffectIds.foldLeft(decrementAbilityCooldownsState) {
        (acc, effectId) =>
          acc.decrementEffectCooldown(effectId)
      }
      decrementEffectCooldownsState
    }

    def refreshCharacterTakenActions(): GameState =
      gs.modify(_.characterIdsThatTookActionThisPhase).setTo(Set.empty)

    def incrementPhase(by: Int = 1): GameState =
      gs.modify(_.phase).using(oldPhase => Phase(oldPhase.number + by))

    def finishPhase()(implicit random: Random, causedById: String = gs.id): GameState =
      refreshCharacterTakenActions()
        .incrementPhase()
        .logEvent(PhaseFinished(gs.generateEventContext()))

    def finishPhaseIfEveryCharacterTookAction()(implicit random: Random): GameState =
      if (gs.charactersToTakeAction.isEmpty) gs.finishPhase()
      else gs

    def putAbilityOnCooldown(abilityId: AbilityId): GameState = {
      val newState = gs.abilityById(abilityId).getCooldownState(gs)
      gs.copy(abilityStates = gs.abilityStates.updated(abilityId, newState))
    }

    def afterAbilityUse(abilityId: AbilityId)(implicit random: Random): GameState = {
      implicit val causedById: String = abilityId

      val ngs = if (gs.abilityCanBeDisabled(abilityId)) gs
      else putAbilityOnCooldownOrDecrementFreeAbility(abilityId)

      ngs.logEvent(AbilityUseFinished(gs.generateEventContext(), abilityId))
    }

    def putAbilityOnCooldownOrDecrementFreeAbility(abilityId: AbilityId)(implicit random: Random): GameState = {
      val ngs = for {
        ability <- gs.abilityByIdOpt(abilityId)
        freeAbilityEffect <- ability.parentCharacter(gs).state.effects.ofType[FreeAbility].headOption
      } yield gs.decrementEffectCooldown(freeAbilityEffect.effectId)

      ngs.getOrElse(putAbilityOnCooldown(abilityId))
    }

    def decrementAbilityCooldown(abilityId: AbilityId, amount: Int = 1): GameState = {
      val newState = gs.abilityById(abilityId).getDecrementCooldownState(amount)(gs)
      gs.copy(abilityStates = gs.abilityStates.updated(abilityId, newState))
    }

    def checkIfCharacterWentInvisible(characterId: CharacterId, wasCharacterInvisible: Boolean)(
        implicit
        random: Random,
        causedById: String,
    ): GameState = {
      val wentInvisible =
        if (wasCharacterInvisible) false
        else gs.characterById(characterId).isInvisible

      if (wentInvisible)
        gs.logEvent(GameEvent.CharacterWentInvisible(gs.generateEventContext(), characterId))
      else gs
    }

    def checkIfCharacterRevealed(characterId: CharacterId, wasCharacterInvisible: Boolean)(
        implicit
        random: Random,
        causedById: String,
    ): GameState = {
      val character = gs.characterById(characterId)
      val wasRevealed =
        if (!wasCharacterInvisible) false
        else !character.isInvisible

      if (wasRevealed)
        gs.reveal(RevealCondition.RelatedCharacterRevealed(characterId))
          .logEvent(GameEvent.CharacterRevealed(
            gs.generateEventContext(),
            characterId,
            character.parentCellOpt(gs).map(_.coordinates),
            character.toView(Some(character.owner(gs).id))(gs).state,
          ))
      else gs
    }

    def knockOutPlayer(playerId: PlayerId)(implicit random: Random, causedById: String): GameState =
      gs.updatePlayer(playerId)(_.modify(_.victoryStatus).setTo(VictoryStatus.Lost))
        .logEvent(PlayerLost(gs.generateEventContext(), playerId))
        .checkVictoryStatus()
        .skipTurnIfPlayerKnockedOut()

    def checkIfPlayerKnockedOut(playerId: PlayerId)(implicit random: Random, causedById: String): GameState =
      if (gs.characters.filter(_.owner(gs).id == playerId).forall(_.isDead)) {
        knockOutPlayer(playerId)
      } else gs

    def handleCharacterDeath(characterId: CharacterId)(implicit random: Random, causedById: String): GameState =
      gs.removeCharacterFromMap(characterId)
        .logEvent(CharacterDied(gs.generateEventContext(), characterId))
        .checkIfPlayerKnockedOut(gs.characterById(characterId).owner(gs).id)

    def checkIfCharacterDied(characterId: CharacterId)(implicit random: Random, causedById: String): GameState =
      if (gs.characterById(characterId).isDead) {
        handleCharacterDeath(characterId)
      } else gs

    def placeCharacter(targetCellCoordinates: HexCoordinates, characterId: CharacterId)(
        implicit
        random: Random,
        causedById: String,
    ): GameState = {
      val ngs = gs.updateHexCell(targetCellCoordinates)(_.copy(characterId = Some(characterId)))
        .modify(_.characterIdsOutsideMap).using(_.filter(_ != characterId))

      val character = gs.characterById(characterId)
      val cpEvent = CharacterPlaced(
        gs.generateEventContext(),
        characterId,
        targetCellCoordinates,
        character.toView(None)(ngs).state,
      )
      if (gs.gameStatus == GameStatus.CharacterPlacing) {
        ngs.logAndHideEvent(
          cpEvent,
          Seq(gs.characterById(characterId).owner(ngs).id),
          RevealCondition.CharacterPlacingFinished,
        )
      } else {
        ngs.logEvent(cpEvent)
      }
    }

    def finishGame()(implicit random: Random, causedById: String): GameState =
      gs.updateGameStatus(GameStatus.Finished)
        .updateClock(gs.clock.pause())(random, gs.id)

    def checkVictoryStatus()(implicit random: Random, causedById: String): GameState = {
      def filterPendingPlayers: Player => Boolean = _.victoryStatus == VictoryStatus.Pending

      val pendingPlayerIds = gs.players.filter(filterPendingPlayers).map(_.id)

      if (gs.gameStatus == GameStatus.CharacterPick && gs.players.count(_.victoryStatus == VictoryStatus.Lost) > 0) {
        gs.modify(_.players.eachWhere(filterPendingPlayers).victoryStatus)
          .setTo(VictoryStatus.Drawn)
          .logEvents(pendingPlayerIds.map(pid => PlayerDrew(gs.generateEventContext(), pid)))
          .finishGame()

      } else if (gs.players.count(_.victoryStatus == VictoryStatus.Pending) == 1) {
        gs.modify(_.players.eachWhere(filterPendingPlayers).victoryStatus)
          .setTo(VictoryStatus.Won)
          .logEvents(pendingPlayerIds.map(pid => PlayerWon(gs.generateEventContext(), pid)))
          .finishGame()
      } else gs
    }

    def checkIfCharacterPickFinished()(implicit random: Random, causedById: String): GameState =
      if (gs.characterPickFinished) {
        gs.updateGameStatus(GameStatus.CharacterPicked)
          .updateClock(gs.clock.setSharedTime(gs.clockConfig.timeAfterPickMillis))(random, gs.id)
          .assignCharactersToPlayers()
          .reveal(RevealCondition.BlindPickFinished)
          .logEvent(CharactersPicked(gs.generateEventContext()))
      } else gs

    def checkIfPlacingCharactersFinished()(implicit random: Random, causedById: String): GameState =
      if (gs.placingCharactersFinished)
        gs.logEvent(PlacingCharactersFinished(gs.generateEventContext()))
          .reveal(RevealCondition.CharacterPlacingFinished)
          .updateGameStatus(GameStatus.Running)
          .updateTimestamp() // timestamp needs to be updated when we change from shared to normal clock
      else gs

  }
}
