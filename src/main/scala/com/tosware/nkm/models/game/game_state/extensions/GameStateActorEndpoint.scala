package com.tosware.nkm.models.game.game_state.extensions

import com.softwaremill.quicklens.*
import com.tosware.nkm.*
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.ability.*
import com.tosware.nkm.models.game.effects.*
import com.tosware.nkm.models.game.event.*
import com.tosware.nkm.models.game.event.GameEvent.*
import com.tosware.nkm.models.game.game_state.{GameState, GameStatus}
import com.tosware.nkm.models.game.hex.*
import com.tosware.nkm.models.game.pick.PickType
import com.tosware.nkm.models.game.pick.blindpick.*
import com.tosware.nkm.models.game.pick.draftpick.*

import scala.util.Random

object GameStateActorEndpoint extends GameStateActorEndpoint
trait GameStateActorEndpoint {
  object General extends General
  trait General {
    implicit class General(gs: GameState) {
      def surrender(playerIds: PlayerId*)(implicit random: Random, causedById: String): GameState = {
        if (gs.gameStatus == GameStatus.Finished) return gs

        def filterPlayers: Player => Boolean = p => playerIds.contains(p.name)

        gs
          .modify(_.players.eachWhere(filterPlayers).victoryStatus).setTo(VictoryStatus.Lost)
          .logEvents(
            playerIds.map(pid => PlayerSurrendered(gs.generateEventContext(), pid)) ++
              playerIds.map(pid => PlayerLost(gs.generateEventContext(), pid))
          )
          .checkVictoryStatus()
          .skipTurnIfPlayerKnockedOut()
      }
    }
  }

  object Initialization extends Initialization
  trait Initialization {
    implicit class Initialization(gs: GameState) {
      def startGame(g: GameStartDependencies)(implicit random: Random): GameState =
        gs.copy(
          hexMap = g.hexMap,
          hexPointGroupOwnerships = g.hexMap.pointGroups.map(g => (g.id, None)).toMap,
          charactersMetadata = g.charactersMetadata,
          players = g.players,
          pickType = g.pickType,
          numberOfBans = g.numberOfBansPerPlayer,
          numberOfCharactersPerPlayers = g.numberOfCharactersPerPlayer,
          gameStatus = if (g.pickType == PickType.AllRandom) GameStatus.CharacterPicked else GameStatus.CharacterPick,
          draftPickStateOpt =
            if (g.pickType == PickType.DraftPick) Some(DraftPickState.empty(DraftPickConfig.generate(g))) else None,
          blindPickStateOpt =
            if (g.pickType == PickType.BlindPick) Some(BlindPickState.empty(BlindPickConfig.generate(g))) else None,
          clockConfig = g.clockConfig,
          clock = Clock.fromConfig(g.clockConfig, playerOrder = g.players.map(_.name)),
        ).initializeCharacterPick()
    }
  }

  object ChampionSelect extends ChampionSelect
  trait ChampionSelect {
    implicit class ChampionSelect(gs: GameState) {
      def ban(playerId: PlayerId, characterIds: Set[CharacterMetadataId])(implicit random: Random): GameState =
        gs.copy(draftPickStateOpt = gs.draftPickStateOpt.map(_.ban(playerId, characterIds)))
          .logAndHideEvent(
            PlayerBanned(gs.generateEventContext()(random, playerId), playerId, characterIds),
            Seq(playerId),
            RevealCondition.BanningPhaseFinished,
          )
          .logEvent(PlayerFinishedBanning(gs.generateEventContext()(random, playerId), playerId))

      def pick(playerId: PlayerId, characterId: CharacterMetadataId)(
          implicit
          random: Random,
          causedById: String,
      ): GameState =
        gs.copy(draftPickStateOpt = gs.draftPickStateOpt.map(_.pick(playerId, characterId)))
          .updateClock(gs.clock.setSharedTime(gs.clockConfig.maxPickTimeMillis))(random, gs.id)
          .logEvent(PlayerPicked(gs.generateEventContext()(random, playerId), playerId, characterId))
          .checkIfCharacterPickFinished()

      def blindPick(playerId: PlayerId, characterIds: Set[CharacterMetadataId])(
          implicit
          random: Random,
          causedById: String,
      ): GameState =
        gs.copy(blindPickStateOpt = gs.blindPickStateOpt.map(_.pick(playerId, characterIds)))
          .logAndHideEvent(
            PlayerBlindPicked(gs.generateEventContext()(random, playerId), playerId, characterIds),
            Seq(playerId),
            RevealCondition.BlindPickFinished,
          )
          .logEvent(PlayerFinishedBlindPicking(gs.generateEventContext()(random, playerId), playerId))
          .checkIfCharacterPickFinished()

    }
  }
  object Time extends Time
  trait Time {
    implicit class Time(gs: GameState) {
      def pause()(implicit random: Random): GameState = {
        val timeToDecrease: Long = gs.millisSinceLastClockUpdate()
        val ngs = if (gs.isSharedTime) {
          gs.decreaseSharedTime(timeToDecrease)
        } else {
          gs.decreaseTime(gs.currentPlayer.id, timeToDecrease)
        }

        ngs.updateClock(ngs.clock.pause())(random, gs.id)
      }

      def unpause()(implicit random: Random): GameState =
        gs.updateClock(gs.clock.unpause())(random, gs.id)
    }
  }

  object Gameplay extends Gameplay
  trait Gameplay {
    implicit class Gameplay(gs: GameState) {
      def placeCharacters(playerId: PlayerId, coordinatesToCharacterIdMap: Map[HexCoordinates, CharacterId])(
          implicit
          random: Random,
          causedById: String,
      ): GameState =
        coordinatesToCharacterIdMap.foldLeft(gs) { case (acc, (coordinate, characterId)) =>
          acc.placeCharacter(coordinate, characterId)(random, playerId)
        }
          .copy(playerIdsThatPlacedCharacters = gs.playerIdsThatPlacedCharacters + playerId)
          .checkIfPlacingCharactersFinished()

      def endTurn()(implicit random: Random, causedById: String = gs.id): GameState =
        gs.characterTakingActionThisTurnOpt match {
          case Some(characterTakingActionThisTurn) =>
            gs
              .logEvent(TurnFinished(gs.generateEventContext(), gs.currentPlayer.id))
              .decreaseTime(gs.currentPlayer.id, gs.millisSinceLastClockUpdate())
              .decrementEndTurnCooldowns()
              .modify(_.characterIdsThatTookActionThisPhase).using(c => c + characterTakingActionThisTurn)
              .modify(_.characterTakingActionThisTurnOpt).setTo(None)
              .incrementTurn()
              .finishPhaseIfEveryCharacterTookAction()
              .skipTurnIfPlayerKnockedOut()
              .skipTurnIfNoCharactersToTakeAction()
              .startTurn()
          case None => gs
        }

      def passTurn(characterId: CharacterId)(implicit random: Random): GameState =
        gs.takeActionWithCharacter(characterId).endTurn()

      def basicMoveCharacter(characterId: CharacterId, path: Seq[HexCoordinates])(implicit
          random: Random
      ): GameState = {
        val ngs = gs.takeActionWithCharacter(characterId)
        gs.characterById(characterId).basicMove(path)(random, ngs)
      }

      def basicAttack(attackingCharacterId: CharacterId, targetCharacterId: CharacterId)(
          implicit random: Random
      ): GameState = {
        implicit val causedById: CharacterId = attackingCharacterId
        val ngs = gs.takeActionWithCharacter(attackingCharacterId)
          .logEvent(CharacterPreparedToAttack(gs.generateEventContext(), attackingCharacterId, targetCharacterId))

        val attackingCharacter = ngs.characterById(attackingCharacterId)
        val targetCharacter = ngs.characterById(targetCharacterId)
        val blockEffects = targetCharacter.state.effects.ofType[Block]
        if (blockEffects.nonEmpty) {
          ngs.removeEffect(blockEffects.head.id)
        } else {
          attackingCharacter.basicAttack(targetCharacterId)(random, ngs)
        }
      }

      def useAbility(abilityId: AbilityId, useData: UseData = UseData())(implicit random: Random): GameState = {
        implicit val causedById: String = abilityId
        val ability = gs.abilityById(abilityId).asInstanceOf[Ability & Usable]
        val parentCharacter = ability.parentCharacter(gs)

        val newGameState = gs.takeActionWithCharacter(parentCharacter.id)
          .logEvent(AbilityUsed(gs.generateEventContext(), abilityId))
        ability.use(useData)(random, newGameState)
          .afterAbilityUse(abilityId)
      }
    }
  }
}
