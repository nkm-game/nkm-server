package com.tosware.NKM.providers

import com.tosware.NKM.models.game.{AbilityMetadata, AbilityType, NKMCharacterMetadata}
import com.tosware.NKM.serializers.NKMJsonProtocol

case class AbilityMetadatasProvider() extends NKMJsonProtocol {
  def getAbilityMetadatas: Seq[AbilityMetadata] = Seq(
    AbilityMetadata(
      abilityType = AbilityType.Passive,
      name = "Nature's Beauty",
      description = "{parentCharacter} can use basic attacks on allies, healing them instead of dealing damage.",
      parentCanAttackAllies = true,
    ),
  )
}
