package com.tosware.nkm.models.game.character

import com.softwaremill.quicklens.*
import com.tosware.nkm.*
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.ability.*
import com.tosware.nkm.models.game.character_effect.{CharacterEffect, CharacterEffectName}
import com.tosware.nkm.models.game.event.GameEvent
import com.tosware.nkm.models.game.event.GameEvent.{CharacterBasicMoved, GameEvent}
import com.tosware.nkm.models.game.hex.*
import com.tosware.nkm.providers.AbilityProvider

import scala.reflect.ClassTag
import scala.util.Random

object NkmCharacter {
  def fromMetadata(characterId: CharacterId, metadata: CharacterMetadata)(implicit random: Random): NkmCharacter = {
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
        purePhysicalDefense = metadata.initialPhysicalDefense,
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
  private val abilityImpairmentCcNames = Seq(CharacterEffectName.Stun, CharacterEffectName.Silence)

  def isDead: Boolean =
    state.healthPoints <= 0

  def isFlying: Boolean =
    state.effects.ofType[effects.Fly].nonEmpty

  def isInvisible: Boolean =
    state.effects.ofType[effects.Invisibility].nonEmpty

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
      .ofType[GameEvent.AbilityUsed]
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
      .map(gameState.abilityById)
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

  def canUseAbilityOfType(abilityType: AbilityType)(implicit gameState: GameState): Boolean =
    (abilityType match {
      case AbilityType.Passive => true
      case AbilityType.Normal => !usedAbilityThisPhase && !usedBasicAttackThisTurn
      case AbilityType.Ultimate => !usedAbilityThisPhase && !usedBasicAttackThisTurn && !usedBasicMoveThisTurn
    }) && !state.effects.exists(e => abilityImpairmentCcNames.contains(e.metadata.name))

  def parentCell(implicit gameState: GameState): Option[HexCell] =
    gameState.hexMap.getCellOfCharacter(id)

  def owner(implicit gameState: GameState): Player =
    gameState.players.find(_.characterIds.contains(id)).get

  def isOnMap(implicit gameState: GameState): Boolean =
    !gameState.characterIdsOutsideMap.contains(id)

  def isEnemyForC(characterId: CharacterId)(implicit gameState: GameState): Boolean =
    isEnemyFor(gameState.characterById(characterId).owner.id)

  def isFriendForC(characterId: CharacterId)(implicit gameState: GameState): Boolean =
    isFriendFor(gameState.characterById(characterId).owner.id)

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

  def defaultBasicMove(path: Seq[HexCoordinates])(implicit random: Random, gameState: GameState): GameState = {
    implicit val causedById: CharacterId = id

    path.tail.foldLeft(gameState)((acc, coordinate) => acc.basicMoveOneCell(id, coordinate)(random, id))
      .logEvent(CharacterBasicMoved(randomUUID(), gameState.phase, gameState.turn, causedById, id, path))
  }

  // case if characterOpt dies on the way? make a test of this and create a new functions with while(onMap)
  def basicMove(path: Seq[HexCoordinates])(implicit random: Random, gameState: GameState): GameState =
    basicMoveOverride.fold(defaultBasicMove(path))(_.basicMove(path))

  def defaultMeleeBasicAttackCells(implicit gameState: GameState): Set[HexCoordinates] =
    parentCell.map { c =>
      c.getArea(
        state.basicAttackRange,
        Set(SearchFlag.StopAtWalls, SearchFlag.StopAfterEnemies, SearchFlag.StopAfterFriends, SearchFlag.StraightLine),
        friendlyPlayerIdOpt = Some(owner.id),
      ).toCoords - c.coordinates
    }
      .getOrElse(Set.empty)
  def defaultRangedBasicAttackCells(implicit gameState: GameState): Set[HexCoordinates] =
    parentCell.map { c =>
      c.getArea(
        state.basicAttackRange,
        Set(SearchFlag.StraightLine),
      ).toCoords - c.coordinates
    }.getOrElse(Set.empty)

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

  def addEffect(effect: CharacterEffect): NkmCharacter =
    this.modify(_.state.effects).using(_ :+ effect)

  def removeEffect(effectId: CharacterEffectId): NkmCharacter =
    this.modify(_.state.effects).using(_.filterNot(_.id == effectId))

  def toView(forPlayer: Option[PlayerId])(implicit gameState: GameState): NkmCharacterView = NkmCharacterView(
    id = id,
    metadataId = metadataId,
    state = state.toView(forPlayer, owner.id),
    ownerId = owner.id,
    isDead = isDead,
    canBasicMove = canBasicMove,
    canBasicAttack = canBasicAttack,
    isOnMap = isOnMap,
    basicAttackCellCoords = basicAttackCellCoords,
    basicAttackTargets = basicAttackTargets,
  )
}
