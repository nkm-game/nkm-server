package com.tosware.NKM.providers

import com.tosware.NKM.models.game._
import com.tosware.NKM.models.game.effects.{DisarmEffect, GroundEffect, SnareEffect, StunEffect}
import com.tosware.NKM.serializers.NKMJsonProtocol

case class CharacterEffectMetadatasProvider() extends NKMJsonProtocol {
  def getCharacterEffectMetadatas: Seq[CharacterEffectMetadata] = Seq(
    GroundEffect.metadata,
    SnareEffect.metadata,
    StunEffect.metadata,
    DisarmEffect.metadata,
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
