package com.tosware.NKM.models.game
import com.tosware.NKM.models.game.effects.{StatBuffEffect, StatNerfEffect}
import com.tosware.NKM.models.game.hex.HexUtils._

case class NKMCharacterState
(
  name: String,
  attackType: AttackType,
  maxHealthPoints: Int,
  healthPoints: Int,
  pureAttackPoints: Int,
  pureBasicAttackRange: Int,
  pureSpeed: Int,
  purePhysicalDefense: Int,
  pureMagicalDefense: Int,
  shield: Int = 0,
  abilities: Seq[Ability] = Seq.empty,
  effects: Seq[CharacterEffect] = Seq.empty,
) {
  private val statBuffs = effects.ofType[StatBuffEffect]
  private val statNerfs = effects.ofType[StatNerfEffect]
  private def calculateEffectModifier(statType: StatType) =
    statBuffs.filter(_.statType == statType).map(_.value).sum - statNerfs.filter(_.statType == statType).map(_.value).sum

  def attackPoints: Int = pureAttackPoints + calculateEffectModifier(StatType.AttackPoints)
  def basicAttackRange: Int = pureBasicAttackRange + calculateEffectModifier(StatType.BasicAttackRange)
  def speed: Int = pureSpeed + calculateEffectModifier(StatType.Speed)
  def physicalDefense: Int = purePhysicalDefense + calculateEffectModifier(StatType.PhysicalDefense)
  def magicalDefense: Int = pureMagicalDefense + calculateEffectModifier(StatType.MagicalDefense)

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
