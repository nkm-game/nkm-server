package com.tosware.nkm.models.game.event

import com.tosware.nkm.models.game.event.GameEvent.GameEvent

case class GameLogView(events: Seq[GameEvent]) extends GameLogLike
