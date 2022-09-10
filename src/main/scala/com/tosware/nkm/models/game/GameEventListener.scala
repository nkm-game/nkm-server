package com.tosware.nkm.models.game

import com.tosware.nkm.models.game.GameEvent.GameEvent

import scala.util.Random

trait GameEventListener {
  def onEvent(e: GameEvent)(implicit random: Random, gameState: GameState): GameState
}
