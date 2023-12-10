package com.tosware.nkm.models.game.ability

import enumeratum.{Enum, EnumEntry}

sealed trait AirSelectionType extends EnumEntry
object AirSelectionType extends Enum[AirSelectionType] {
  val values = findValues

  case object None extends AirSelectionType
  case object Circular extends AirSelectionType
}
