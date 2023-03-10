package com.tosware.nkm.models.game.character

import com.tosware.nkm._

object CharacterMetadata {
  def empty(name: String = "Empty"): CharacterMetadata =
    CharacterMetadata(name, AttackType.Melee, 100, 10, 5, 5, 10, 10, Seq.empty)
}


case class CharacterMetadata
(
  name: String,
  attackType: AttackType,
  initialHealthPoints: Int,
  initialAttackPoints: Int,
  initialBasicAttackRange: Int,
  initialSpeed: Int,
  initialPhysicalDefense: Int,
  initialMagicalDefense: Int,
  initialAbilitiesMetadataIds: Seq[AbilityMetadataId],
) {
  val id: CharacterMetadataId = name
}

