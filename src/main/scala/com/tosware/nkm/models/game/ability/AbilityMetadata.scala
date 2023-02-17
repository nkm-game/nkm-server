package com.tosware.nkm.models.game.ability

import com.tosware.nkm.models.game.character_effect.CharacterEffect.CharacterEffectId
import com.tosware.nkm.models.game.ability.Ability.AbilityMetadataId

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
