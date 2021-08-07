package com.tosware.NKM.models.game

import enumeratum._

sealed trait PickType extends EnumEntry
object PickType extends Enum[PickType] {
  val values = findValues

  case object AllRandom extends PickType
  case object DraftPick extends PickType
  case object BlindPick extends PickType
}

