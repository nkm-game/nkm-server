package com.tosware.nkm.providers

import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.effects._
import com.tosware.nkm.serializers.NkmJsonProtocol

case class CharacterEffectMetadatasProvider() extends NkmJsonProtocol {
  def getCharacterEffectMetadatas: Seq[CharacterEffectMetadata] = Seq(
    BlackBlood.metadata,
    Disarm.metadata,
    Fly.metadata,
    FreeAbility.metadata,
    Ground.metadata,
    HasToTakeAction.metadata,
    Poison.metadata,
    ManipulatorOfObjectsImmunity.metadata,
    MurasamePoison.metadata,
    Silence.metadata,
    Snare.metadata,
    StatBuff.metadata,
    StatNerf.metadata,
    Stun.metadata,
  )
}
