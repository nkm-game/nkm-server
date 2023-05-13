package com.tosware.nkm.models.game.hex_effect

import enumeratum.EnumEntry.CapitalWords
import enumeratum.*

sealed trait HexCellEffectType extends EnumEntry with CapitalWords
object HexCellEffectType extends Enum[HexCellEffectType] {
  val values: IndexedSeq[HexCellEffectType] = findValues

  case object Trap extends HexCellEffectType
}

