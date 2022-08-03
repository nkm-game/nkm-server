package com.tosware.NKM.models.game

import CharacterMetadata._
import com.tosware.NKM.models.game.Ability.AbilityMetadataId

object CharacterMetadata {
  def empty(name: String = "Empty"): CharacterMetadata =
    CharacterMetadata(name, AttackType.Melee, 100, 10, 5, 5, 10, 10, Seq.empty)
  type CharacterMetadataId = String
}


case class CharacterMetadata
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

