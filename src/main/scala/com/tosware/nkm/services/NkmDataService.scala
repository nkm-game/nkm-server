package com.tosware.nkm.services

import com.tosware.nkm.NkmTimeouts
import com.tosware.nkm.models.game.ability.AbilityMetadata
import com.tosware.nkm.models.game.character.CharacterMetadata
import com.tosware.nkm.models.game.character_effect.CharacterEffectMetadata
import com.tosware.nkm.models.game.hex.HexMapTemplate
import com.tosware.nkm.models.game.hex_effect.HexCellEffectMetadata
import com.tosware.nkm.providers.*

class NkmDataService extends NkmTimeouts
{
  def getHexMaps: Seq[HexMapTemplate] = HexMapProvider().getHexMaps

  def getCharacterMetadatas: Seq[CharacterMetadata] = CharacterMetadatasProvider().getCharacterMetadatas
    .filter(_.initialAbilitiesMetadataIds.size >= 3)

  def getAbilityMetadatas: Seq[AbilityMetadata] = AbilityMetadatasProvider().getAbilityMetadatas

  def getCharacterEffectMetadatas: Seq[CharacterEffectMetadata] = CharacterEffectMetadatasProvider().getCharacterEffectMetadatas

  def getHexCellEffectMetadatas: Seq[HexCellEffectMetadata] = HexCellEffectMetadatasProvider().getHexCellEffectMetadatas

  def getCurrentVersion: String = scala.io.Source.fromResource("version.txt").mkString.trim
}
