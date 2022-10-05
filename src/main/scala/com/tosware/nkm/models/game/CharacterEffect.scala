package com.tosware.nkm.models.game

import com.tosware.nkm.models.game.CharacterEffect._
import com.tosware.nkm.models.game.hex.HexCell
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
  case object HasToTakeAction extends CharacterEffectName
  case object Poison extends CharacterEffectName
  case object Fly extends CharacterEffectName
  case object Disarm extends CharacterEffectName
  case object Silence extends CharacterEffectName
  case object StatBuff extends CharacterEffectName
  case object StatNerf extends CharacterEffectName

  case object MurasamePoison extends CharacterEffectName
  case object BlackBlood extends CharacterEffectName
}

sealed trait CharacterEffectType extends EnumEntry
object CharacterEffectType extends Enum[CharacterEffectType] {
  val values: IndexedSeq[CharacterEffectType] = findValues

  case object Positive extends CharacterEffectType
  case object Neutral extends CharacterEffectType
  case object Negative extends CharacterEffectType
  case object Mixed extends CharacterEffectType
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

  def parentCharacter(implicit gameState: GameState): NkmCharacter =
    gameState.characters.find(_.state.effects.map(_.id).contains(id)).get
  def parentCell(implicit gameState: GameState): Option[HexCell] =
    parentCharacter.parentCell
}

case class CharacterEffectState(cooldown: Int)
