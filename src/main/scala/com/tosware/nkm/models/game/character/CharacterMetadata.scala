package com.tosware.nkm.models.game.character

import com.tosware.nkm.*

object CharacterMetadata {
  def empty(name: String = "Empty"): CharacterMetadata =
    CharacterMetadata(name, AttackType.Melee, 100, 10, 5, 5, 10, 10, Seq.empty)

  def withAbility(abilityMetadataId: AbilityMetadataId): CharacterMetadata =
    empty().copy(initialAbilitiesMetadataIds = Seq(abilityMetadataId))
}

case class CharacterMetadata(
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
