package com.tosware.nkm.models.game.event

import com.tosware.nkm.*
import com.tosware.nkm.models.game.event.GameEvent.{CharacterTookAction, GameEvent}

trait GameLogLike {
  val events: Seq[GameEvent]
  def characterThatTookActionInTurn(turnNumber: Int): Option[CharacterId] =
    events
      .ofType[CharacterTookAction]
      .inTurn(turnNumber)
      .headOption
      .map(_.characterId)
}
