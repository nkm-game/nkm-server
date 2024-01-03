package com.tosware.nkm.models.game.event

import com.tosware.nkm.*
import com.tosware.nkm.models.game.event.GameEvent.GameEvent
import com.tosware.nkm.models.game.game_state.GameState

case class GameLog(events: Seq[GameEvent]) extends GameLogLike {
  def toView(forPlayerOpt: Option[PlayerId])(implicit gameState: GameState): GameLogView = {
    val eventsFiltered = events.filterNot(e => gameState.hiddenEidsFor(forPlayerOpt).contains(e.context.id))
    GameLogView(eventsFiltered)
  }
}
