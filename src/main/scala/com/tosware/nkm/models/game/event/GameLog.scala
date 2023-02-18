package com.tosware.nkm.models.game.event

import com.tosware.nkm.models.game.GameState
import com.tosware.nkm.models.game.Player.PlayerId
import com.tosware.nkm.models.game.event.GameEvent.GameEvent

case class GameLog(events: Seq[GameEvent]) extends GameLogLike {
  def toView(forPlayerOpt: Option[PlayerId])(implicit gameState: GameState): GameLogView = {
    val eventsFiltered = events.filterNot(e => gameState.hiddenEidsFor(forPlayerOpt).contains(e.id))
    GameLogView(eventsFiltered)
  }
}
