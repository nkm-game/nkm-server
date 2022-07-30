package com.tosware.NKM.models.game

case class NKMCharacterState
(
  name: String,
  attackType: AttackType,
  healthPoints: Int,
  attackPoints: Int,
  basicAttackRange: Int,
  speed: Int,
  psychicalDefense: Int,
  magicalDefense: Int,
  abilities: Seq[Ability] = Seq.empty,
  effects: Seq[CharacterEffect] = Seq.empty,
) {
  def toView: NKMCharacterStateView = NKMCharacterStateView(
    name = name,
    attackType: AttackType,
    healthPoints = healthPoints,
    attackPoints = attackPoints,
    basicAttackRange = basicAttackRange,
    speed = speed,
    psychicalDefense = psychicalDefense,
    magicalDefense = magicalDefense,
    abilities = abilities.map(_.state),
    effects = effects.map(_.state),
  )
}


case class NKMCharacterStateView
(
  name: String,
  attackType: AttackType,
  healthPoints: Int,
  attackPoints: Int,
  basicAttackRange: Int,
  speed: Int,
  psychicalDefense: Int,
  magicalDefense: Int,
  abilities: Seq[AbilityState] = Seq.empty,
  effects: Seq[CharacterEffectState] = Seq.empty,
)
