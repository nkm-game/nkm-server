package com.tosware.NKM.models.game

case class NKMCharacterState
(
  name: String,
  attackType: AttackType,
  maxHealthPoints: Int,
  healthPoints: Int,
  attackPoints: Int,
  basicAttackRange: Int,
  speed: Int,
  physicalDefense: Int,
  magicalDefense: Int,
  shield: Int = 0,
  abilities: Seq[Ability] = Seq.empty,
  effects: Seq[CharacterEffect] = Seq.empty,
) {
  def toView: NKMCharacterStateView = NKMCharacterStateView(
    name = name,
    attackType: AttackType,
    maxHealthPoints = maxHealthPoints,
    healthPoints = healthPoints,
    attackPoints = attackPoints,
    basicAttackRange = basicAttackRange,
    speed = speed,
    physicalDefense = physicalDefense,
    magicalDefense = magicalDefense,
    abilities = abilities.map(_.state),
    effects = effects.map(_.state),
  )
}

case class NKMCharacterStateView
(
  name: String,
  attackType: AttackType,
  maxHealthPoints: Int,
  healthPoints: Int,
  attackPoints: Int,
  basicAttackRange: Int,
  speed: Int,
  physicalDefense: Int,
  magicalDefense: Int,
  abilities: Seq[AbilityState] = Seq.empty,
  effects: Seq[CharacterEffectState] = Seq.empty,
)
