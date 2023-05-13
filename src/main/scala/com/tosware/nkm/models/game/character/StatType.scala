package com.tosware.nkm.models.game.character

import enumeratum.*

sealed trait StatType extends EnumEntry
object StatType extends Enum[StatType] {
  val values = findValues

  case object AttackPoints extends StatType
  case object BasicAttackRange extends StatType
  case object Speed extends StatType
  case object PhysicalDefense extends StatType
  case object MagicalDefense extends StatType
}
