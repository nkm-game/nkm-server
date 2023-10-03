package com.tosware.nkm.models.game

import enumeratum.*

sealed trait GameStatus extends EnumEntry
object GameStatus extends Enum[GameStatus] {
  val values = findValues

  case object NotStarted extends GameStatus
  case object CharacterPick extends GameStatus
  case object CharacterPicked extends GameStatus
  case object CharacterPlacing extends GameStatus
  case object Running extends GameStatus
  case object Finished extends GameStatus
}
