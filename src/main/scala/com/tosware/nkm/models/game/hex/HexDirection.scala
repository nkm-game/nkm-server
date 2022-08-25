package com.tosware.nkm.models.game.hex

import enumeratum.{Enum, EnumEntry}

sealed trait HexDirection extends EnumEntry
object HexDirection extends Enum[HexDirection] {
  val values = findValues

  case object NE extends HexDirection
  case object E extends HexDirection
  case object SE extends HexDirection
  case object SW extends HexDirection
  case object W extends HexDirection
  case object NW extends HexDirection
}
