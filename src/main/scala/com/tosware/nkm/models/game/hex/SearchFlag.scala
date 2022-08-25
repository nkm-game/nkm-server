package com.tosware.nkm.models.game.hex

import enumeratum.{Enum, EnumEntry}

sealed trait SearchFlag extends EnumEntry
object SearchFlag extends Enum[SearchFlag] {
  val values = findValues

  case object StopAtWalls extends SearchFlag
  case object StopAtEnemies extends SearchFlag
  case object StopAtFriends extends SearchFlag
  case object StopAfterEnemies extends SearchFlag
  case object StopAfterFriends extends SearchFlag
  case object StraightLine extends SearchFlag
}

