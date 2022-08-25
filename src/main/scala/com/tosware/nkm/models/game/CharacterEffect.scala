package com.tosware.nkm.models.game

import com.tosware.nkm.models.game.CharacterEffect._
import enumeratum._

object CharacterEffect {
  type CharacterEffectId = String
  type CharacterEffectMetadataId = String
}

sealed trait CharacterEffectName extends EnumEntry
object CharacterEffectName extends Enum[CharacterEffectName] {
  val values: IndexedSeq[CharacterEffectName] = findValues

  case object Snare extends CharacterEffectName
  case object Stun extends CharacterEffectName
  case object Ground extends CharacterEffectName
  case object Poison extends CharacterEffectName
  case object Fly extends CharacterEffectName
  case object Disarm extends CharacterEffectName
  case object Silence extends CharacterEffectName
  case object StatBuff extends CharacterEffectName
  case object StatNerf extends CharacterEffectName
}

sealed trait CharacterEffectType extends EnumEntry
object CharacterEffectType extends Enum[CharacterEffectType] {
  val values: IndexedSeq[CharacterEffectType] = findValues

  case object Positive extends CharacterEffectType
  case object Neutral extends CharacterEffectType
  case object Negative extends CharacterEffectType
}

case class CharacterEffectMetadata
(
  name: CharacterEffectName,
  effectType: CharacterEffectType,
  description: String,
  isCc: Boolean = false,
) {
  def id: CharacterEffectMetadataId = name.toString
}

abstract class CharacterEffect(val id: CharacterEffectId) {
  val metadata: CharacterEffectMetadata
  val cooldown: Int
  val state: CharacterEffectState = CharacterEffectState(cooldown)
}

case class CharacterEffectState(cooldown: Int)
