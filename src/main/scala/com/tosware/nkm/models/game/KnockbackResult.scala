package com.tosware.nkm.models.game

import enumeratum.{Enum, EnumEntry}

sealed trait KnockbackResult extends EnumEntry
object KnockbackResult extends Enum[KnockbackResult] {
  val values = findValues

  case object HitNothing extends KnockbackResult
  case object HitWall extends KnockbackResult
  case object HitEndOfMap extends KnockbackResult
  case object HitCharacter extends KnockbackResult
}
