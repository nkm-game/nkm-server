package com.tosware.NKM.models.game

import enumeratum._

sealed trait GamePhase extends EnumEntry
object GamePhase extends Enum[GamePhase] {
  val values = findValues

  case object NotStarted extends GamePhase
  case object CharacterPick extends GamePhase
  case object CharacterPlacing extends GamePhase
  case object Running extends GamePhase
  case object Finished extends GamePhase
}


