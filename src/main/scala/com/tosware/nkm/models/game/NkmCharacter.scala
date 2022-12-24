package com.tosware.nkm.models.game

import com.softwaremill.quicklens._
import com.tosware.nkm.models.game.CharacterEffect.CharacterEffectId
import com.tosware.nkm.models.game.CharacterMetadata.CharacterMetadataId
import com.tosware.nkm.models.game.GameEvent.GameEvent
import com.tosware.nkm.models.game.NkmCharacter._
import com.tosware.nkm.models.game.Player.PlayerId
import com.tosware.nkm.models.game.hex.HexUtils._
import com.tosware.nkm.models.game.hex._
import com.tosware.nkm.models.{Damage, DamageType}
import com.tosware.nkm.providers.AbilityProvider

import scala.reflect.ClassTag
import scala.util.Random

object NkmCharacter {
  type CharacterId = String

  def fromMetadata(characterId: CharacterId, metadata: CharacterMetadata)(implicit random: Random) = {
    NkmCharacter(
      id = characterId,
      metadataId = metadata.id,
      state = NkmCharacterState(
        name = metadata.name,
        attackType = metadata.attackType,
        maxHealthPoints = metadata.initialHealthPoints,
        healthPoints = metadata.initialHealthPoints,
        pureAttackPoints = metadata.initialAttackPoints,
        pureBasicAttackRange = metadata.initialBasicAttackRange,
        pureSpeed = metadata.initialSpeed,
        purePhysicalDefense = metadata.initialPsychicalDefense,
        pureMagicalDefense = metadata.initialMagicalDefense,
        abilities = AbilityProvider.instantiateAbilities(characterId, metadata.initialAbilitiesMetadataIds),
      )
    )
  }
}

case class NkmCharacter
(
  id: CharacterId,
  metadataId: CharacterMetadataId,
  state: NkmCharacterState,
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

  private def getUsedAbilitiesThisPhase(implicit gameState: GameState): Seq[Ability] = {
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
  }

  def usedAbilityThisPhase(implicit gameState: GameState): Boolean =
    getUsedAbilitiesThisPhase.nonEmpty

  def usedUltimatumAbilityThisPhase(implicit gameState: GameState): Boolean = {
    getUsedAbilitiesThisPhase
      .map(_.metadata.abilityType)
      .contains(AbilityType.Ultimate)
  }

  private def hasRefreshed[RefreshEvent <: GameEvent: ClassTag, ActionEvent <: GameEvent: ClassTag](implicit gameState: GameState): Boolean = {
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

  def canBasicMove(implicit gameState: GameState): Boolean = {
    (hasRefreshedBasicMove || !usedBasicMoveThisTurn && !usedUltimatumAbilityThisPhase) &&
      !state.effects.exists(e => basicMoveImpairmentCcNames.contains(e.metadata.name))
  }

  def canBasicAttack(implicit gameState: GameState): Boolean =
    (hasRefreshedBasicAttack || !usedBasicAttackThisTurn && !usedAbilityThisPhase) &&
      !state.effects.exists(e => basicAttackImpairmentCcNames.contains(e.metadata.name))

  def parentCell(implicit gameState: GameState): Option[HexCell] =
    gameState.hexMap.get.getCellOfCharacter(id)

  def owner(implicit gameState: GameState): Player =
    gameState.players.find(_.characterIds.contains(id)).get

  def isOnMap(implicit gameState: GameState): Boolean =
    !gameState.characterIdsOutsideMap.contains(id)

  def isEnemyForC(characterId: CharacterId)(implicit gameState: GameState): Boolean =
    isEnemyFor(gameState.characterById(characterId).get.owner.id)

  def isFriendForC(characterId: CharacterId)(implicit gameState: GameState): Boolean =
    isFriendFor(gameState.characterById(characterId).get.owner.id)

  def isEnemyFor(playerId: PlayerId)(implicit gameState: GameState): Boolean =
    playerId != owner.id

  def isFriendFor(playerId: PlayerId)(implicit gameState: GameState): Boolean =
    playerId == owner.id

  def basicAttackOverride: Option[BasicAttackOverride] =
    state.abilities.ofType[BasicAttackOverride].headOption

  def basicAttackCellCoords(implicit gameState: GameState): Set[HexCoordinates] =
    basicAttackOverride.fold(defaultBasicAttackCells)(_.basicAttackCells)

  def basicAttackTargets(implicit gameState: GameState): Set[HexCoordinates] =
    basicAttackOverride.fold(defaultBasicAttackTargets)(_.basicAttackTargets)

  def basicAttack(targetCharacterId: CharacterId)(implicit random: Random, gameState: GameState): GameState =
    basicAttackOverride.fold(defaultBasicAttack(targetCharacterId))(_.basicAttack(targetCharacterId))

  def basicMoveOverride: Option[BasicMoveOverride] =
    state.abilities.ofType[BasicMoveOverride].headOption

  def defaultBasicMove(path: Seq[HexCoordinates])(implicit random: Random, gameState: GameState): GameState =
    path.tail.foldLeft(gameState)((acc, coordinate) => acc.teleportCharacter(id, coordinate)(random, id))

  // case if character dies on the way? make a test of this and create a new functions with while(onMap)
  def basicMove(path: Seq[HexCoordinates])(implicit random: Random, gameState: GameState): GameState =
    basicMoveOverride.fold(defaultBasicMove(path))(_.basicMove(path))



  def defaultMeleeBasicAttackCells(implicit gameState: GameState): Set[HexCoordinates] = {
    if(parentCell.isEmpty) return Set.empty
    val range = state.basicAttackRange
    parentCell.get.getArea(
      range,
      Set(SearchFlag.StopAtWalls, SearchFlag.StopAfterEnemies, SearchFlag.StopAfterFriends, SearchFlag.StraightLine),
      friendlyPlayerIdOpt = Some(owner.id),
    ).toCoords
  }
  def defaultRangedBasicAttackCells(implicit gameState: GameState): Set[HexCoordinates] = {
    if(parentCell.isEmpty) return Set.empty
    val range = state.basicAttackRange
    parentCell.get.getArea(range, Set(SearchFlag.StraightLine)).toCoords
  }

  def defaultBasicAttackCells(implicit gameState: GameState): Set[HexCoordinates] = {
    state.attackType match {
      case AttackType.Melee =>
        defaultMeleeBasicAttackCells
      case AttackType.Ranged =>
        defaultRangedBasicAttackCells
    }
  }

  def defaultBasicAttackTargets(implicit gameState: GameState): Set[HexCoordinates] =
    basicAttackCellCoords.whereEnemiesOfC(id)

  def defaultBasicAttack(targetCharacterId: CharacterId)(implicit random: Random, gameState: GameState): GameState =
    gameState.damageCharacter(targetCharacterId, Damage(DamageType.Physical, state.attackPoints))(random, id)

  def heal(amount: Int): NkmCharacter =
    this.modify(_.state.healthPoints).using(oldHp => math.min(oldHp + amount, state.maxHealthPoints))

  def calculateReduction(damage: Damage): Int = {
    val defense = damage.damageType match {
      case DamageType.Physical => state.physicalDefense
      case DamageType.Magical => state.magicalDefense
      case DamageType.True => 0
    }
    (damage.amount * defense / 100f).toInt
  }

  def receiveDamage(damage: Damage): NkmCharacter = {
    val reduction = calculateReduction(damage)
    val damageAfterReduction: Int = damage.amount - reduction
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

  def addEffect(effect: CharacterEffect): NkmCharacter =
    this.modify(_.state.effects).using(_ :+ effect)

  def removeEffect(effectId: CharacterEffectId): NkmCharacter =
    this.modify(_.state.effects).using(_.filterNot(_.id == effectId))

  def toView(implicit gameState: GameState): NkmCharacterView = NkmCharacterView(
    id = id,
    metadataId = metadataId,
    state = state.toView,
    ownerId = owner.id,
    isDead = isDead,
    canBasicMove = canBasicMove,
    canBasicAttack = canBasicAttack,
    isOnMap = isOnMap,
    basicAttackCellCoords = basicAttackCellCoords,
    basicAttackTargets = basicAttackTargets,
  )
}

case class NkmCharacterView
(
  id: CharacterId,
  metadataId: CharacterMetadataId,
  state: NkmCharacterStateView,
  ownerId: PlayerId,
  isDead: Boolean,
  canBasicMove: Boolean,
  canBasicAttack: Boolean,
  isOnMap: Boolean,
  basicAttackCellCoords: Set[HexCoordinates],
  basicAttackTargets: Set[HexCoordinates],
)
