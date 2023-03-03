package com.tosware.nkm.models.game.hex_effect

import enumeratum.EnumEntry.CapitalWords
import enumeratum._

sealed trait HexCellEffectName extends EnumEntry with CapitalWords
object HexCellEffectName extends Enum[HexCellEffectName] {
  val values: IndexedSeq[HexCellEffectName] = findValues

  case object MarkOfTheWind extends HexCellEffectName
}
