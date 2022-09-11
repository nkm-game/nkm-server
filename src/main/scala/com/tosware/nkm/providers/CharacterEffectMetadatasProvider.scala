package com.tosware.nkm.providers

import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.effects._
import com.tosware.nkm.serializers.NkmJsonProtocol

case class CharacterEffectMetadatasProvider() extends NkmJsonProtocol {
  def getCharacterEffectMetadatas: Seq[CharacterEffectMetadata] = Seq(
    BlackBlood.metadata,
    Disarm.metadata,
    Ground.metadata,
    Silence.metadata,
    Snare.metadata,
    StatBuff.metadata,
    StatNerf.metadata,
    Stun.metadata,
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
  )
}
