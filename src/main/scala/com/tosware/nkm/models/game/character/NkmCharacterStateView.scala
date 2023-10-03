package com.tosware.nkm.models.game.character

import com.tosware.nkm.*

case class NkmCharacterStateView(
    name: String,
    attackType: AttackType,
    maxHealthPoints: Int,
    healthPoints: Int,
    attackPoints: Int,
    basicAttackRange: Int,
    speed: Int,
    physicalDefense: Int,
    magicalDefense: Int,
    shield: Int,
    abilities: Seq[AbilityId] = Seq.empty,
    effects: Seq[CharacterEffectId] = Seq.empty,
)
