package com.tosware.NKM.models.game

import NKMCharacterMetadata._
import com.tosware.NKM.models.game.Ability.AbilityMetadataId

object NKMCharacterMetadata {
  def empty(name: String = "Empty"): NKMCharacterMetadata =
    NKMCharacterMetadata(name, AttackType.Melee, 100, 10, 5, 5, 10, 10, Seq.empty)
  type CharacterMetadataId = String
}


case class NKMCharacterMetadata
(
  name: String,
  attackType: AttackType,
  initialHealthPoints: Int,
  initialAttackPoints: Int,
  initialBasicAttackRange: Int,
  initialSpeed: Int,
  initialPsychicalDefense: Int,
  initialMagicalDefense: Int,
  initialAbilitiesMetadataIds: Seq[AbilityMetadataId],
) {
  val id: CharacterMetadataId = name
}

