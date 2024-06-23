package com.tosware.nkm.models.game

import enumeratum.{Enum, EnumEntry}

sealed trait GameMode extends EnumEntry
object GameMode extends Enum[GameMode] {
  val values = findValues

  case object Deathmatch extends GameMode
  case object CaptureThePoint extends GameMode
}
