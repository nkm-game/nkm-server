package com.tosware.NKM.models.game

import com.softwaremill.quicklens._
import com.tosware.NKM.models.game.Ability.AbilityMetadataId
import com.tosware.NKM.models.game.CharacterEffect.CharacterEffectId
import com.tosware.NKM.models.game.CharacterMetadata.CharacterMetadataId
import com.tosware.NKM.models.game.GameEvent.GameEvent
import com.tosware.NKM.models.game.NKMCharacter._
import com.tosware.NKM.models.game.abilities.aqua.{NaturesBeauty, Purification, Resurrection}
import com.tosware.NKM.models.game.abilities.hecate.{Aster, MasterThrone, PowerOfExistence}
import com.tosware.NKM.models.game.abilities.llenn.{GrenadeThrow, PChan, RunItDown}
import com.tosware.NKM.models.game.abilities.sinon.{PreciseShot, SnipersSight, TacticalEscape}
import com.tosware.NKM.models.game.hex.HexUtils._
import com.tosware.NKM.models.game.hex._
import com.tosware.NKM.models.{Damage, DamageType}

import scala.reflect.ClassTag
import scala.util.Random

object NKMCharacter {
  type CharacterId = String
  def instantiateAbilities(characterId: CharacterId, metadataIds: Seq[AbilityMetadataId])(implicit random: Random): Seq[Ability] = {
    metadataIds.map {
      case NaturesBeauty.metadata.id =>
        NaturesBeauty(NKMUtils.randomUUID, characterId)
      case Purification.metadata.id =>
        Purification(NKMUtils.randomUUID, characterId)
      case Resurrection.metadata.id =>
        Resurrection(NKMUtils.randomUUID, characterId)

      case MasterThrone.metadata.id =>
        MasterThrone(NKMUtils.randomUUID, characterId)
      case Aster.metadata.id =>
        Aster(NKMUtils.randomUUID, characterId)
      case PowerOfExistence.metadata.id =>
        PowerOfExistence(NKMUtils.randomUUID, characterId)

      case SnipersSight.metadata.id =>
        SnipersSight(NKMUtils.randomUUID, characterId)
      case TacticalEscape.metadata.id =>
        TacticalEscape(NKMUtils.randomUUID, characterId)
      case PreciseShot.metadata.id =>
        PreciseShot(NKMUtils.randomUUID, characterId)

      case PChan.metadata.id =>
        PChan(NKMUtils.randomUUID, characterId)
      case GrenadeThrow.metadata.id =>
        GrenadeThrow(NKMUtils.randomUUID, characterId)
      case RunItDown.metadata.id =>
        RunItDown(NKMUtils.randomUUID, characterId)
    }
  }

  def fromMetadata(characterId: CharacterId, metadata: CharacterMetadata)(implicit random: Random) = {
    NKMCharacter(
      id = characterId,
      metadataId = metadata.id,
      state = NKMCharacterState(
        name = metadata.name,
        attackType = metadata.attackType,
        maxHealthPoints = metadata.initialHealthPoints,
        healthPoints = metadata.initialHealthPoints,
        pureAttackPoints = metadata.initialAttackPoints,
        pureBasicAttackRange = metadata.initialBasicAttackRange,
        pureSpeed = metadata.initialSpeed,
        purePhysicalDefense = metadata.initialPsychicalDefense,
        pureMagicalDefense = metadata.initialMagicalDefense,
        abilities = instantiateAbilities(characterId, metadata.initialAbilitiesMetadataIds)
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
  private val basicMoveImpairmentCcNames = Seq(CharacterEffectName.Stun, CharacterEffectName.Ground, CharacterEffectName.Snare)
  private val basicAttackImpairmentCcNames = Seq(CharacterEffectName.Stun, CharacterEffectName.Disarm)

  def isDead: Boolean = state.healthPoints <= 0

  def usedBasicMoveThisTurn(implicit gameState: GameState): Boolean =
    gameState.gameLog.events
      .inTurn(gameState.turn.number)
      .ofType[GameEvent.CharacterBasicMoved]
      .ofCharacter(id)
      .nonEmpty

  def usedBasicAttackThisTurn(implicit gameState: GameState): Boolean =
    gameState.gameLog.events
    .inTurn(gameState.turn.number)
    .ofType[GameEvent.CharacterBasicAttacked]
    .ofCharacter(id)
    .nonEmpty

  def hasRefreshed[RefreshEvent <: GameEvent: ClassTag, ActionEvent <: GameEvent: ClassTag](implicit gameState: GameState): Boolean = {
    val lastRefreshIndex = gameState.gameLog.events
      .ofType[RefreshEvent]
      .inTurn(gameState.turn.number)
      .ofCharacter(id)
      .map(_.index)
      .lastOption

    val lastActionIndex = gameState.gameLog.events
      .ofType[ActionEvent]
      .inTurn(gameState.turn.number)
      .ofCharacter(id)
      .map(_.index)
      .lastOption

    lastRefreshIndex.isDefined && (lastActionIndex.isEmpty || lastRefreshIndex.get > lastActionIndex.get)

  }

  def hasRefreshedBasicMove(implicit gameState: GameState): Boolean =
    hasRefreshed[GameEvent.BasicMoveRefreshed, GameEvent.CharacterBasicMoved]

  def hasRefreshedBasicAttack(implicit gameState: GameState): Boolean =
    hasRefreshed[GameEvent.BasicAttackRefreshed, GameEvent.CharacterBasicAttacked]

  def usedUltimatumAbilityThisPhase(implicit gameState: GameState): Boolean =
    (
    gameState.gameLog.events
      .inPhase(gameState.phase.number)
      .ofType[GameEvent.AbilityUsedWithoutTarget]
    ++
    gameState.gameLog.events
      .inPhase(gameState.phase.number)
      .ofType[GameEvent.AbilityUsedOnCoordinates]
    ++
    gameState.gameLog.events
      .inPhase(gameState.phase.number)
      .ofType[GameEvent.AbilityUsedOnCharacter]
    )
    .map(_.abilityId)
    .map(aid => gameState.abilityById(aid).get)
    .filter(a => a.parentCharacter.id == id)
    .map(_.metadata.abilityType)
    .contains(AbilityType.Ultimate)

  def canBasicMove(implicit gameState: GameState): Boolean = {
    (hasRefreshedBasicMove || !usedBasicMoveThisTurn && !usedUltimatumAbilityThisPhase) &&
      !state.effects.exists(e => basicMoveImpairmentCcNames.contains(e.metadata.name))
  }

  def canBasicAttack(implicit gameState: GameState): Boolean =
    (hasRefreshedBasicAttack || !usedBasicAttackThisTurn && !usedUltimatumAbilityThisPhase) &&
      !state.effects.exists(e => basicAttackImpairmentCcNames.contains(e.metadata.name))

  def parentCell(implicit gameState: GameState): Option[HexCell] =
    gameState.hexMap.get.getCellOfCharacter(id)

  def owner(implicit gameState: GameState): Player =
    gameState.players.find(_.characterIds.contains(id)).get

  def isOnMap(implicit gameState: GameState): Boolean =
    !gameState.characterIdsOutsideMap.contains(id)

  def isEnemyFor(characterId: CharacterId)(implicit gameState: GameState): Boolean =
    gameState.characterById(characterId).get.owner != owner

  def isFriendFor(characterId: CharacterId)(implicit gameState: GameState): Boolean =
    gameState.characterById(characterId).get.owner == owner

  def basicAttackOverride: Option[BasicAttackOverride] =
    state.abilities.collectFirst {
      case o: BasicAttackOverride => o
    }

  def basicAttackCellCoords(implicit gameState: GameState): Set[HexCoordinates] =
    basicAttackOverride.fold(defaultBasicAttackCells)(_.basicAttackCells)

  def basicAttackTargets(implicit gameState: GameState): Set[HexCoordinates] =
    basicAttackOverride.fold(defaultBasicAttackTargets)(_.basicAttackTargets)

  def basicAttack(targetCharacterId: CharacterId)(implicit random: Random, gameState: GameState): GameState =
    basicAttackOverride.fold(defaultBasicAttack(targetCharacterId))(_.basicAttack(targetCharacterId))

  def defaultBasicAttackCells(implicit gameState: GameState): Set[HexCoordinates] = {
    if(parentCell.isEmpty) return Set.empty
    val parentCoordinates = parentCell.get.coordinates
    state.attackType match {
      case AttackType.Melee => parentCoordinates.getLines(HexDirection.values.toSet, state.basicAttackRange).whereExists // TODO: stop at walls and characters
      case AttackType.Ranged => parentCoordinates.getLines(HexDirection.values.toSet, state.basicAttackRange).whereExists
    }
  }

  def defaultBasicAttackTargets(implicit gameState: GameState): Set[HexCoordinates] =
    basicAttackCellCoords.whereEnemiesOf(id)

  def defaultBasicAttack(targetCharacterId: CharacterId)(implicit random: Random, gameState: GameState): GameState =
    gameState.damageCharacter(targetCharacterId, Damage(DamageType.Physical, state.attackPoints))(random, id)

  def heal(amount: Int): NKMCharacter =
    this.modify(_.state.healthPoints).using(oldHp => math.min(oldHp + amount, state.maxHealthPoints))

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
    this.modify(_.state.effects).using(_ :+ effect)

  def removeEffect(effectId: CharacterEffectId): NKMCharacter =
    this.modify(_.state.effects).using(_.filterNot(_.id == effectId))

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
