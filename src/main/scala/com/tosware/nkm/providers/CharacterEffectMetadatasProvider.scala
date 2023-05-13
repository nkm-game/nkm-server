package com.tosware.nkm.providers

import com.tosware.nkm.models.game.character_effect.*
import com.tosware.nkm.models.game.effects.*
import com.tosware.nkm.serializers.NkmJsonProtocol

case class CharacterEffectMetadatasProvider() extends NkmJsonProtocol {
  def getCharacterEffectMetadatas: Seq[CharacterEffectMetadata] = Seq(
    AbilityEnchant.metadata,
    AbilityUnlock.metadata,
    ApplyEffectOnBasicAttack.metadata,
    BlackBlood.metadata,
    Block.metadata,
    Disarm.metadata,
    Fly.metadata,
    FreeAbility.metadata,
    Ground.metadata,
    HasToTakeAction.metadata,
    HealOverTime.metadata,
    Invisibility.metadata,
    ManipulatorOfObjectsImmunity.metadata,
    MurasamePoison.metadata,
    NextBasicAttackBuff.metadata,
    Poison.metadata,
    Silence.metadata,
    Snare.metadata,
    StatBuff.metadata,
    StatNerf.metadata,
    Stun.metadata,
    ZeroGravity.metadata,
  )
}
