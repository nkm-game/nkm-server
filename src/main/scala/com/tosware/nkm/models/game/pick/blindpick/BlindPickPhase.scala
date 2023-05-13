package com.tosware.nkm.models.game.pick.blindpick

import enumeratum.*

sealed trait BlindPickPhase extends EnumEntry

object BlindPickPhase extends Enum[BlindPickPhase] {
  val values = findValues

  case object Picking extends BlindPickPhase
  case object Finished extends BlindPickPhase
}





