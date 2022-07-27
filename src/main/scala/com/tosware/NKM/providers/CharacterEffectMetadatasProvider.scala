package com.tosware.NKM.providers

import com.tosware.NKM.models.game._
import com.tosware.NKM.serializers.NKMJsonProtocol

case class CharacterEffectMetadatasProvider() extends NKMJsonProtocol {
  def getCharacterEffectMetadatas: Seq[CharacterEffectMetadata] = Seq(
    CharacterEffectMetadata(
      name = CharacterEffectName.Snare,
      effectType = CharacterEffectType.Negative,
      description = "This character cannot basic move.",
      isCc = true,
    ),
    CharacterEffectMetadata(
      name = CharacterEffectName.Stun,
      effectType = CharacterEffectType.Negative,
      description = "This character cannot take action.",
      isCc = true,
    ),
    CharacterEffectMetadata(
      name = CharacterEffectName.Ground,
      effectType = CharacterEffectType.Negative,
      description = "This character is grounded and cannot move.",
      isCc = true,
    ),
    CharacterEffectMetadata(
      name = CharacterEffectName.Poison,
      effectType = CharacterEffectType.Negative,
      description = "Deals damage every time this character takes action.",
    ),
    CharacterEffectMetadata(
      name = CharacterEffectName.Fly,
      effectType = CharacterEffectType.Positive,
      description = "This character can fly.",
    ),
    CharacterEffectMetadata(
      name = CharacterEffectName.Disarm,
      effectType = CharacterEffectType.Negative,
      description = "This character is disarmed and cannot use basic attacks.",
      isCc = true,
    ),
    CharacterEffectMetadata(
      name = CharacterEffectName.Silence,
      effectType = CharacterEffectType.Negative,
      description = "This character is silenced and cannot use abilities.",
      isCc = true,
    ),
    CharacterEffectMetadata(
      name = CharacterEffectName.StatBuff,
      effectType = CharacterEffectType.Positive,
      description = "Buffs a certain stat of character.",
    ),
    CharacterEffectMetadata(
      name = CharacterEffectName.StatNerf,
      effectType = CharacterEffectType.Negative,
      description = "Nerfs a certain stat of character.",
    ),
  )
}
