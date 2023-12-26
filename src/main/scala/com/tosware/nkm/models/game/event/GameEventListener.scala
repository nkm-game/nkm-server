package com.tosware.nkm.models.game.event

import com.tosware.nkm.models.game.event.GameEvent.GameEvent
import com.tosware.nkm.models.game.game_state.GameState

import scala.util.Random

trait GameEventListener {
  def onEvent(e: GameEvent)(implicit random: Random, gameState: GameState): GameState
}
