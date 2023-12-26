package com.tosware.nkm.models.game.game_state.extensions

import com.tosware.nkm.*
import com.tosware.nkm.models.game.game_state.GameState

import scala.util.Random

object GameStateTimeManagement extends GameStateTimeManagement
trait GameStateTimeManagement {
  implicit class GameStateTimeManagment(gs: GameState) {
    def decreaseSharedTime(timeMillis: Long)(implicit random: Random): GameState =
      gs.updateClock(gs.clock.setIsSharedTime(true).decreaseSharedTime(timeMillis))(random, gs.id)

    def decreaseTime(playerId: PlayerId, timeMillis: Long)(implicit random: Random): GameState =
      gs.updateClock(gs.clock.setIsSharedTime(false).decreaseTime(playerId, timeMillis))(random, playerId)

    def increaseTime(playerId: PlayerId, timeMillis: Long)(implicit random: Random): GameState =
      gs.updateClock(gs.clock.increaseTime(playerId, timeMillis))(random, playerId)

  }
}
