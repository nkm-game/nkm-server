package com.tosware.NKM.models.game

import com.softwaremill.quicklens._
import NKMCharacter._
import NKMCharacterMetadata.CharacterMetadataId
import com.tosware.NKM.models.{Damage, DamageType}
import com.tosware.NKM.models.game.hex._

object NKMCharacter {
  type CharacterId = String
  def fromMetadata(characterId: CharacterId, metadata: NKMCharacterMetadata) = {
    NKMCharacter(
      id = characterId,
      metadataId = metadata.id,
      state = NKMCharacterState(
        name = metadata.name,
        attackType = metadata.attackType,
        healthPoints = metadata.initialHealthPoints,
        attackPoints = metadata.initialAttackPoints,
        basicAttackRange = metadata.initialBasicAttackRange,
        speed = metadata.initialSpeed,
        physicalDefense = metadata.initialPsychicalDefense,
        magicalDefense = metadata.initialMagicalDefense,
      )
    )
  }
}

case class NKMCharacter
(
  id: CharacterId,
  metadataId: CharacterMetadataId,
  state: NKMCharacterState,
)
{
  val basicMoveImpairmentCcNames = Seq(CharacterEffectName.Stun, CharacterEffectName.Ground, CharacterEffectName.Snare)
  val basicAttackImpairmentCcNames = Seq(CharacterEffectName.Stun, CharacterEffectName.Disarm)

  def canBasicMove: Boolean = !state.effects.exists(e => basicMoveImpairmentCcNames.contains(e.metadata.name))

  def canBasicAttack: Boolean = !state.effects.exists(e => basicAttackImpairmentCcNames.contains(e.metadata.name))

  def basicAttackCells(implicit gameState: GameState): Set[HexCoordinates] = defaultGetBasicAttackCells

  def parentCell(implicit gameState: GameState): Option[HexCell] =
    gameState.hexMap.get.getCellOfCharacter(id)

  def owner(implicit gameState: GameState): Player =
    gameState.players.find(_.characterIds.contains(id)).get

  def defaultGetBasicAttackCells(implicit gameState: GameState): Set[HexCoordinates] = {
    if(parentCell.isEmpty) return Set.empty
    val parentCoordinates = parentCell.get.coordinates
    state.attackType match {
      case AttackType.Melee => parentCoordinates.getLines(HexDirection.values.toSet, state.basicAttackRange) // TODO: stop at walls and characters
      case AttackType.Ranged => parentCoordinates.getLines(HexDirection.values.toSet, state.basicAttackRange)
    }
  }

  def receiveDamage(damage: Damage): NKMCharacter = {
    val defense = damage.damageType match {
      case DamageType.Physical => state.physicalDefense
      case DamageType.Magical => state.magicalDefense
      case DamageType.True => 0
    }

    val reduction = damage.amount * defense / 100f
    val damageAfterReduction: Int = damage.amount - reduction.toInt
    if (damageAfterReduction <= 0) return this

    if(state.shield >= damageAfterReduction) {
      val newShield = state.shield - damageAfterReduction
      this.modify(_.state.shield).setTo(newShield)
    }
    else {
      val damageAfterShield = damageAfterReduction - state.shield
      val newShield = 0
      val newHp = state.healthPoints - damageAfterShield
      this.modify(_.state.shield).setTo(newShield)
        .modify(_.state.healthPoints).setTo(newHp)
    }

  }

  def addEffect(effect: CharacterEffect): NKMCharacter =
    this.modify(_.state.effects).using(e => e :+ effect)

  def toView: NKMCharacterView = NKMCharacterView(
    id = id,
    metadataId = metadataId,
    state = state.toView,
  )
}

case class NKMCharacterView
(
  id: CharacterId,
  metadataId: CharacterMetadataId,
  state: NKMCharacterStateView,
)
