package com.tosware.nkm.models.game.character

import com.tosware.nkm.*
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.ability.Ability
import com.tosware.nkm.models.game.character_effect.CharacterEffect
import com.tosware.nkm.models.game.effects.*

case class NkmCharacterState(
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
  private val statBuffs = effects.ofType[StatBuff]
  private val statNerfs = effects.ofType[StatNerf]
  private def calculateEffectModifier(statType: StatType) =
    statBuffs.filter(_.statType == statType).map(_.value).sum - statNerfs.filter(_.statType == statType).map(
      _.value
    ).sum

  def attackPoints: Int = pureAttackPoints + calculateEffectModifier(StatType.AttackPoints)
  def basicAttackRange: Int = pureBasicAttackRange + calculateEffectModifier(StatType.BasicAttackRange)
  def speed: Int = pureSpeed + calculateEffectModifier(StatType.Speed)
  def physicalDefense: Int = purePhysicalDefense + calculateEffectModifier(StatType.PhysicalDefense)
  def magicalDefense: Int = pureMagicalDefense + calculateEffectModifier(StatType.MagicalDefense)

  def missingHp: Int = maxHealthPoints - healthPoints
  def currentHpPercent: Int = healthPoints * 100 / maxHealthPoints
  def missingHpPercent: Int = missingHp * 100 / maxHealthPoints

  def toView(forPlayer: Option[PlayerId], ownerId: PlayerId): Option[NkmCharacterStateView] =
    if (effects.ofType[Invisibility].nonEmpty && !forPlayer.contains(ownerId)) None
    else Some(
      character.NkmCharacterStateView(
        name = name,
        attackType: AttackType,
        maxHealthPoints = maxHealthPoints,
        healthPoints = healthPoints,
        attackPoints = attackPoints,
        basicAttackRange = basicAttackRange,
        speed = speed,
        physicalDefense = physicalDefense,
        magicalDefense = magicalDefense,
        shield = shield,
        abilities = abilities.map(_.id),
        effects = effects.map(_.id),
      )
    )
}
