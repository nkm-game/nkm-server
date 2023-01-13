package com.tosware.nkm.models

import com.tosware.nkm.actors.Game.GameId
import com.tosware.nkm.models.game.GameEvent.GameEvent

case class GameEventMapped(gameId: GameId, event: GameEvent)
