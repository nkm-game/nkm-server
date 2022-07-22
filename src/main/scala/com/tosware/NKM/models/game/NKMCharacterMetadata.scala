package com.tosware.NKM.models.game

import NKMCharacterMetadata._

object NKMCharacterMetadata {
  def empty(name: String = "Empty"): NKMCharacterMetadata = NKMCharacterMetadata(name, 100, 10, 5, 5, 10, 10, Seq.empty)
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

