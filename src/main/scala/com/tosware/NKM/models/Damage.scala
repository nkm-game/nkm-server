package com.tosware.NKM.models

import com.tosware.NKM.models.game.NKMCharacter.CharacterId
import enumeratum.{Enum, EnumEntry}

sealed trait DamageType extends EnumEntry
object DamageType extends Enum[DamageType] {
  val values = findValues

  case object Physical extends DamageType
  case object Magical extends DamageType
  case object True extends DamageType
}

case class Damage(sourceId: CharacterId, damageType: DamageType, amount: Int)
