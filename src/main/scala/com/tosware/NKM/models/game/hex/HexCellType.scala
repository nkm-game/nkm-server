package com.tosware.NKM.models.game.hex

import enumeratum.{Enum, EnumEntry}

sealed trait HexCellType extends EnumEntry
object HexCellType extends Enum[HexCellType] {
  val values = findValues

  case object Transparent extends HexCellType
  case object Normal extends HexCellType
  case object Wall extends HexCellType
  case object SpawnPoint extends HexCellType
}

