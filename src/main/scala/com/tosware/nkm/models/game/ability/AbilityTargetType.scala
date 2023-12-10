package com.tosware.nkm.models.game.ability

import enumeratum.{Enum, EnumEntry}

object AbilityTargetType extends Enum[AbilityTargetType] {
  val values = findValues

  case object None extends AbilityTargetType
  case object Character extends AbilityTargetType
  case object DeadCharacter extends AbilityTargetType
  case object HexCoordinates extends AbilityTargetType
}

sealed trait AbilityTargetType extends EnumEntry
