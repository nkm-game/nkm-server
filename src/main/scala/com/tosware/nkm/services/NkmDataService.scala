package com.tosware.nkm.services

import com.tosware.nkm.NkmTimeouts
import com.tosware.nkm.models.game.ability.AbilityMetadata
import com.tosware.nkm.models.game.character.CharacterMetadata
import com.tosware.nkm.models.game.character_effect.CharacterEffectMetadata
import com.tosware.nkm.models.game.hex.HexMap
import com.tosware.nkm.providers._

class NkmDataService extends NkmTimeouts
{
  def getHexMaps: Seq[HexMap] = HexMapProvider().getHexMaps

  def getCharacterMetadatas: Seq[CharacterMetadata] = CharacterMetadatasProvider().getCharacterMetadatas
    .filter(_.initialAbilitiesMetadataIds.size >= 3)

  def getAbilityMetadatas: Seq[AbilityMetadata] = AbilityMetadatasProvider().getAbilityMetadatas

  def getCharacterEffectMetadatas: Seq[CharacterEffectMetadata] = CharacterEffectMetadatasProvider().getCharacterEffectMetadatas

  def getCurrentVersion: String = scala.io.Source.fromResource("version.txt").mkString.trim
}
