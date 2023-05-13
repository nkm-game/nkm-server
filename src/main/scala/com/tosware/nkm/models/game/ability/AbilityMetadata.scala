package com.tosware.nkm.models.game.ability

import com.tosware.nkm.*

case class AbilityMetadata
(
  name: String,
  abilityType: AbilityType,
  description: String,
  variables: Map[String, Int] = Map.empty,
  alternateName: String = "",
  relatedEffectIds: Seq[CharacterEffectId] = Seq.empty,
) {
  val id: AbilityMetadataId = name
}
