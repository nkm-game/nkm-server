package com.tosware.nkm.models.game.game_state.extensions

import com.tosware.nkm.*
import com.tosware.nkm.models.game.event.*
import com.tosware.nkm.models.game.event.GameEvent.*
import com.tosware.nkm.models.game.game_state.{GameState, GameStatus}
import com.tosware.nkm.models.game.pick.PickType

import scala.util.Random

object GameStateActorTimeouts extends GameStateActorTimeouts
trait GameStateActorTimeouts {

  implicit class GameStateActorTimeouts(gs: GameState) {

    /** Timeout ID to handle clock timeout in Game Actor
      */
    def timeoutNumber: Int = gs.gameStatus match {
      case GameStatus.NotStarted => 0
      case GameStatus.CharacterPick | GameStatus.CharacterPicked =>
        gs.pickType match {
          case PickType.AllRandom => 0
          case PickType.DraftPick => gs.draftPickStateOpt.fold(0)(_.pickNumber)
          case PickType.BlindPick => gs.blindPickStateOpt.fold(0)(_.pickNumber)
        }
      case GameStatus.CharacterPlacing | GameStatus.Running | GameStatus.Finished =>
        gs.turn.number
    }

    def finishBanningPhase()(implicit random: Random): GameState = {
      if (gs.gameStatus == GameStatus.Finished) return gs

      gs.copy(draftPickStateOpt = gs.draftPickStateOpt.map(_.finishBanning()))
        .updateClock(gs.clock.setSharedTime(gs.clockConfig.maxPickTimeMillis))(random, gs.id)
        .reveal(RevealCondition.BanningPhaseFinished)
        .logEvent(BanningPhaseFinished(randomUUID(), gs.phase, gs.turn, gs.id))
    }

    def draftPickTimeout()(implicit random: Random, causedById: String): GameState = {
      if (gs.gameStatus == GameStatus.Finished) return gs

      val currentPlayerIdOpt = for {
        draftPickState <- gs.draftPickStateOpt
        currentPlayerPicking <- draftPickState.currentPlayerPicking
      } yield currentPlayerPicking
      currentPlayerIdOpt.fold(gs)(currentPlayerId => gs.surrender(currentPlayerId))
    }

    def blindPickTimeout()(implicit random: Random, causedById: String): GameState = {
      if (gs.gameStatus == GameStatus.Finished) return gs
      gs.surrender(gs.blindPickStateOpt.map(_.pickingPlayers).getOrElse(Seq.empty)*)
    }

    def placingCharactersTimeout()(implicit random: Random, causedById: String): GameState = {
      if (gs.gameStatus == GameStatus.Finished) return gs

      val pidsThatDidNotPlace: Set[PlayerId] = gs.players.map(_.id).toSet -- gs.playerIdsThatPlacedCharacters
      gs.placeCharactersRandomly(pidsThatDidNotPlace)
    }

    def startPlacingCharacters()(implicit random: Random, causedById: String): GameState = {
      if (gs.gameStatus == GameStatus.Finished) return gs

      gs.updateGameStatus(GameStatus.CharacterPlacing)
        .initializeCharacterPlacing()
        .pickAndPlaceCharactersRandomlyIfAllRandom()
    }

  }
}
