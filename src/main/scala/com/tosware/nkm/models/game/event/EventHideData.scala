package com.tosware.nkm.models.game.event

import com.tosware.nkm.models.game.Player.PlayerId
import com.tosware.nkm.models.game.event.GameEvent.GameEventId

case class EventHideData(eid: GameEventId, showOnlyFor: Seq[PlayerId], revealCondition: RevealCondition)

