package com.tosware.nkm.models.game.event

import com.tosware.nkm.models.game.GameState
import com.tosware.nkm.models.game.event.GameEvent.GameEvent

import scala.util.Random

trait GameEventListener {
  def onEvent(e: GameEvent)(implicit random: Random, gameState: GameState): GameState
}
