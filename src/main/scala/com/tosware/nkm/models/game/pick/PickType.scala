package com.tosware.nkm.models.game.pick

import enumeratum.*

sealed trait PickType extends EnumEntry
object PickType extends Enum[PickType] {
  val values = findValues

  case object AllRandom extends PickType
  case object DraftPick extends PickType
  case object BlindPick extends PickType
}

