package com.tosware.nkm.services

import com.tosware.nkm.models.game.{AbilityMetadata, CharacterEffectMetadata, CharacterMetadata}
import com.tosware.nkm.providers._
import com.tosware.nkm.NkmTimeouts
import com.tosware.nkm.models.game.hex.HexMap

class NkmDataService extends NkmTimeouts
{
  def getHexMaps: Seq[HexMap] = HexMapProvider().getHexMaps

  def getCharacterMetadatas: Seq[CharacterMetadata] = CharacterMetadatasProvider().getCharacterMetadatas

  def getAbilityMetadatas: Seq[AbilityMetadata] = AbilityMetadatasProvider().getAbilityMetadatas

  def getCharacterEffectMetadatas: Seq[CharacterEffectMetadata] = CharacterEffectMetadatasProvider().getCharacterEffectMetadatas
}
