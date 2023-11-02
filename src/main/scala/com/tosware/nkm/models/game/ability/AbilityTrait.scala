package com.tosware.nkm.models.game.ability

import enumeratum.*

sealed trait AbilityTrait extends EnumEntry
object AbilityTrait extends Enum[AbilityTrait] {
  val values = findValues

  case object Move extends AbilityTrait
  case object ContactFriend extends AbilityTrait
  case object ContactEnemy extends AbilityTrait
}
