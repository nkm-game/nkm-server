package com.tosware.nkm.models.game.event

import com.tosware.nkm.*

case class EventHideData(eid: GameEventId, showOnlyFor: Seq[PlayerId], revealCondition: RevealCondition)
