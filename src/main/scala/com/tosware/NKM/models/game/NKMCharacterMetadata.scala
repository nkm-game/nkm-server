package com.tosware.NKM.models.game

import NKMCharacterMetadata._

object NKMCharacterMetadata {
  type CharacterMetadataId = String
}
case class NKMCharacterMetadata
(
  name: String,
  initialHealthPoints: Int,
  initialAttackPoints: Int,
  initialBasicAttackRange: Int,
  initialSpeed: Int,
  initialPsychicalDefense: Int,
  initialMagicalDefense: Int,
  initialAbilitiesMetadataIds: Seq[String],
) {
  val id: CharacterMetadataId = name
}

