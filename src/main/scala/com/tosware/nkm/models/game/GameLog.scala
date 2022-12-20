package com.tosware.nkm.models.game

import com.tosware.nkm.models.game.GameEvent.GameEvent

case class GameLog(events: Seq[GameEvent])

