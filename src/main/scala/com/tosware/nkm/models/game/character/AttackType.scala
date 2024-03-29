package com.tosware.nkm.models.game.character

import enumeratum.{Enum, EnumEntry}

sealed trait AttackType extends EnumEntry
object AttackType extends Enum[AttackType] {
  val values = findValues

  case object Melee extends AttackType
  case object Ranged extends AttackType
}
