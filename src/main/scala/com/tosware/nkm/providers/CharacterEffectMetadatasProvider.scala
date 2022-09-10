package com.tosware.nkm.providers

import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.effects.{Disarm, Ground, Snare, Stun}
import com.tosware.nkm.serializers.NkmJsonProtocol

case class CharacterEffectMetadatasProvider() extends NkmJsonProtocol {
  def getCharacterEffectMetadatas: Seq[CharacterEffectMetadata] = Seq(
    Ground.metadata,
    Snare.metadata,
    Stun.metadata,
    Disarm.metadata,
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
