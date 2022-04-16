package com.tosware.NKM.models.game

import enumeratum._

sealed trait VictoryStatus extends EnumEntry
object VictoryStatus extends Enum[VictoryStatus] {
  val values = findValues

  case object Pending extends VictoryStatus
  case object Lost extends VictoryStatus
  case object Won extends VictoryStatus
  case object Drawn extends VictoryStatus
}



