package com.tosware.nkm.models.game.ability

import com.tosware.nkm.*
object AbilityMetadata {
  def apply(
      name: String,
      abilityType: AbilityType,
      description: String,
      alternateName: String = "",
      relatedEffectIds: Seq[CharacterEffectId] = Seq.empty,
      traits: Seq[AbilityTrait] = Seq.empty,
      targetsMetadata: Seq[AbilityTargetMetadata] = Seq.empty,
  )(implicit path: NkmConf.AutoExtract.Path): AbilityMetadata =
    AbilityMetadata(
      name,
      abilityType,
      description,
      alternateName,
      relatedEffectIds,
      traits,
      NkmConf.extract(path.value),
      targetsMetadata,
    )
}

case class AbilityMetadata(
    name: String,
    abilityType: AbilityType,
    description: String,
    alternateName: String,
    relatedEffectIds: Seq[CharacterEffectId],
    traits: Seq[AbilityTrait],
    variables: Map[String, Int],
    targetsMetadata: Seq[AbilityTargetMetadata],
) {
  val id: AbilityMetadataId = name

  def toMarshallable: AbilityMetadataMarshallable = AbilityMetadataMarshallable(
    name,
    abilityType,
    description,
    alternateName,
    relatedEffectIds,
    traits,
    variables,
    targetsMetadata.map(_.toMarshallable),
  )
}

case class AbilityMetadataMarshallable(
    name: String,
    abilityType: AbilityType,
    description: String,
    alternateName: String,
    relatedEffectIds: Seq[CharacterEffectId],
    traits: Seq[AbilityTrait],
    variables: Map[String, Int],
    targetsMetadata: Seq[AbilityTargetMetadataMarshallable],
) {
  val id: AbilityMetadataId = name
}
