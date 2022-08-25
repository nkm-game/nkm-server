package com.tosware.nkm.models.game.draftpick

import enumeratum._

sealed trait DraftPickPhase extends EnumEntry
object DraftPickPhase extends Enum[DraftPickPhase] {
  val values = findValues

  case object Banning extends DraftPickPhase
  case object Picking extends DraftPickPhase
  case object Finished extends DraftPickPhase
}




