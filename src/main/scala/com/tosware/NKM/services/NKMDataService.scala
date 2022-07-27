package com.tosware.NKM.services

import com.tosware.NKM.models.game.{AbilityMetadata, CharacterEffectMetadata, HexMap, NKMCharacterMetadata}
import com.tosware.NKM.providers._
import com.tosware.NKM.NKMTimeouts

class NKMDataService extends NKMTimeouts
{
  def getHexMaps: Seq[HexMap] = HexMapProvider().getHexMaps

  def getCharacterMetadatas: Seq[NKMCharacterMetadata] = CharacterMetadatasProvider().getCharacterMetadatas

  def getAbilityMetadatas: Seq[AbilityMetadata] = AbilityMetadatasProvider().getAbilityMetadatas

  def getCharacterEffectMetadatas: Seq[CharacterEffectMetadata] = CharacterEffectMetadatasProvider().getCharacterEffectMetadatas
}
