package com.tosware.nkm.models.game.character_effect

import enumeratum.EnumEntry.CapitalWords
import enumeratum.*

sealed trait CharacterEffectName extends EnumEntry with CapitalWords
object CharacterEffectName extends Enum[CharacterEffectName] {
  val values: IndexedSeq[CharacterEffectName] = findValues

  case object AbilityUnlock extends CharacterEffectName
  case object AbilityEnchant extends CharacterEffectName
  case object ApplyEffectOnBasicAttack extends CharacterEffectName
  case object BlackBlood extends CharacterEffectName
  case object Block extends CharacterEffectName
  case object Disarm extends CharacterEffectName
  case object NextBasicAttackBuff extends CharacterEffectName
  case object Fly extends CharacterEffectName
  case object FreeAbility extends CharacterEffectName
  case object Ground extends CharacterEffectName
  case object HasToTakeAction extends CharacterEffectName
  case object HealOverTime extends CharacterEffectName
  case object Invisibility extends CharacterEffectName
  case object ManipulatorOfObjectsImmunity extends CharacterEffectName
  case object MurasamePoison extends CharacterEffectName
  case object Poison extends CharacterEffectName
  case object Silence extends CharacterEffectName
  case object Snare extends CharacterEffectName
  case object StatBuff extends CharacterEffectName
  case object StatNerf extends CharacterEffectName
  case object Stun extends CharacterEffectName
  case object ZeroGravity extends CharacterEffectName
}
