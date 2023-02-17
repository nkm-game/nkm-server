package com.tosware.nkm.models.game.character_effect

import enumeratum._

sealed trait CharacterEffectType extends EnumEntry
object CharacterEffectType extends Enum[CharacterEffectType] {
  val values: IndexedSeq[CharacterEffectType] = findValues

  case object Positive extends CharacterEffectType
  case object Neutral extends CharacterEffectType
  case object Negative extends CharacterEffectType
  case object Mixed extends CharacterEffectType
}

