package com.tosware.nkm.models

import com.tosware.nkm.actors.Game.GameId
import com.tosware.nkm.models.game.event.EventHideData
import com.tosware.nkm.models.game.event.GameEvent.GameEvent

case class GameEventMapped(gameId: GameId, event: GameEvent, hideData: Option[EventHideData])
