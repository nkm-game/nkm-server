package com.tosware.nkm.models.game.ability

import com.tosware.nkm.*
object AbilityMetadata {
  def apply(
      name: String,
      abilityType: AbilityType,
      description: String,
      alternateName: String = "",
      relatedEffectIds: Seq[CharacterEffectId] = Seq.empty,
  )(implicit path: NkmConf.AutoExtract.Path): AbilityMetadata =
    AbilityMetadata(
      name,
      abilityType,
      description,
      alternateName,
      relatedEffectIds,
      NkmConf.extract(path.value),
    )
}

case class AbilityMetadata(
    name: String,
    abilityType: AbilityType,
    description: String,
    alternateName: String,
    relatedEffectIds: Seq[CharacterEffectId],
    variables: Map[String, Int],
) {
  val id: AbilityMetadataId = name
}
