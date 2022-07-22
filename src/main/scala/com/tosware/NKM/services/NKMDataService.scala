package com.tosware.NKM.services

import com.tosware.NKM.models.game.{HexMap, NKMCharacterMetadata}
import com.tosware.NKM.{CharactersMetadataProvider, HexMapProvider, NKMTimeouts}

class NKMDataService extends NKMTimeouts
{
  def getHexMaps: Seq[HexMap] = HexMapProvider().getHexMaps

  def getCharactersMetadata: Seq[NKMCharacterMetadata] = CharactersMetadataProvider().getCharactersMetadata
}
