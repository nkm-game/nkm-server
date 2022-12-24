package com.tosware.nkm.models.game

import com.tosware.nkm.models.game.CharacterEffect._
import com.tosware.nkm.models.game.NkmCharacter.CharacterId
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
  case object FreeAbility extends CharacterEffectName

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
  initialEffectType: CharacterEffectType,
  description: String,
  isCc: Boolean = false,
) {
  def id: CharacterEffectMetadataId = name.toString
}

abstract class CharacterEffect(val id: CharacterEffectId) {
  val metadata: CharacterEffectMetadata
  val initialCooldown: Int

  def effectType(implicit gameState: GameState): CharacterEffectType =
    metadata.initialEffectType
  def state(implicit gameState: GameState): CharacterEffectState =
    gameState.characterEffectStates(id)
  def parentCharacter(implicit gameState: GameState): NkmCharacter =
    gameState.characters.find(_.state.effects.map(_.id).contains(id)).get
  def parentCell(implicit gameState: GameState): Option[HexCell] =
    parentCharacter.parentCell

  def getDecrementCooldownState(implicit gameState: GameState): CharacterEffectState =
    state.copy(cooldown = math.max(state.cooldown - 1, 0))

  def toView(implicit gameState: GameState): CharacterEffectView =
    CharacterEffectView(
      id = id,
      metadataId = metadata.id,
      parentCharacterId = parentCharacter.id,
      state = state,
      effectType = effectType,
    )
}

case class CharacterEffectState(cooldown: Int)

case class CharacterEffectView
(
  id: CharacterEffectId,
  metadataId: CharacterEffectMetadataId,
  parentCharacterId: CharacterId,
  state: CharacterEffectState,
  effectType: CharacterEffectType,
)
