package com.tosware.NKM.models

import enumeratum.{Enum, EnumEntry}

sealed trait DamageType extends EnumEntry
object DamageType extends Enum[DamageType] {
  val values = findValues

  case object Physical extends DamageType
  case object Magical extends DamageType
  case object True extends DamageType
}

case class Damage(damageType: DamageType, amount: Int)
