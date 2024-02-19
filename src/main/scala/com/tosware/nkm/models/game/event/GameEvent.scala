package com.tosware.nkm.models.game.event

import com.tosware.nkm.*
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.ability.UseData
import com.tosware.nkm.models.game.character.*
import com.tosware.nkm.models.game.game_state.{GameState, GameStatus}
import com.tosware.nkm.models.game.hex.HexCoordinates

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object GameEvent {
  case class GameEventContext(id: GameEventId, phase: Phase, turn: Turn, causedById: String, time: ZonedDateTime) {
    override def toString: String = {
      val shortId = id.take(5)
      val formatter = DateTimeFormatter.ofPattern("ss.SSS")
      val formattedTime = time.format(formatter)

      s"Ctx($shortId, $phase, $turn, $causedById, $formattedTime)"
    }
  }

  trait ContainsCharacterId {
    val characterId: CharacterId
  }

  trait ContainsAbilityId {
    val abilityId: AbilityId
  }

  sealed trait GameEvent {
    def context: GameEventContext
    def index(implicit gameState: GameState): Int =
      gameState.gameLog.events.indexWhere(_.context.id == context.id)

    def id: GameEventId = context.id
    def phase: Phase = context.phase
    def turn: Turn = context.turn
    def causedById: PlayerId = context.causedById
    def time: ZonedDateTime = context.time
  }

  case class GameStatusUpdated(context: GameEventContext, newGameStatus: GameStatus) extends GameEvent
  case class EventsRevealed(context: GameEventContext, eventIds: Seq[GameEventId]) extends GameEvent
  case class CharacterWentInvisible(context: GameEventContext, characterId: CharacterId) extends GameEvent
      with ContainsCharacterId
  case class CharacterRevealed(
      context: GameEventContext,
      characterId: CharacterId,
      revealedOnCoordinates: Option[HexCoordinates],
      characterState: Option[NkmCharacterStateView],
  ) extends GameEvent with ContainsCharacterId
  case class ClockUpdated(context: GameEventContext, newClock: Clock) extends GameEvent
  case class CharacterPlaced(
      context: GameEventContext,
      characterId: CharacterId,
      target: HexCoordinates,
      characterState: Option[NkmCharacterStateView],
  ) extends GameEvent with ContainsCharacterId
  case class EffectAddedToCell(
      context: GameEventContext,
      hexCellEffectId: HexCellEffectId,
      target: HexCoordinates,
  ) extends GameEvent
  case class EffectRemovedFromCell(context: GameEventContext, hexCellEffectId: HexCellEffectId) extends GameEvent
  case class EffectAddedToCharacter(
      context: GameEventContext,
      effectMetadataId: CharacterEffectMetadataId,
      effectId: CharacterEffectId,
      characterId: CharacterId,
  ) extends GameEvent with ContainsCharacterId
  case class EffectRemovedFromCharacter(
      context: GameEventContext,
      effectMetadataId: CharacterEffectMetadataId,
      effectId: CharacterEffectId,
      characterId: CharacterId,
  ) extends GameEvent with ContainsCharacterId
  case class EffectVariableSet(
      context: GameEventContext,
      effectId: CharacterEffectId,
      key: String,
      value: String,
  ) extends GameEvent
  case class AbilityHitCharacter(context: GameEventContext, abilityId: AbilityId, targetCharacterId: CharacterId)
      extends GameEvent with ContainsAbilityId
  case class AbilityUsed(context: GameEventContext, abilityId: AbilityId, useData: UseData) extends GameEvent
      with ContainsAbilityId
  case class AbilityUseFinished(context: GameEventContext, abilityId: AbilityId) extends GameEvent
      with ContainsAbilityId
  case class AbilityVariableSet(context: GameEventContext, abilityId: AbilityId, key: String, value: String)
      extends GameEvent with ContainsAbilityId
  case class CharacterBasicMoved(context: GameEventContext, characterId: CharacterId, path: Seq[HexCoordinates])
      extends GameEvent with ContainsCharacterId
  case class MovementInterrupted(context: GameEventContext, characterId: CharacterId) extends GameEvent
      with ContainsCharacterId
  case class CharacterPreparedToAttack(
      context: GameEventContext,
      characterId: CharacterId,
      targetCharacterId: CharacterId,
  ) extends GameEvent with ContainsCharacterId
  case class CharacterBasicAttacked(
      context: GameEventContext,
      characterId: CharacterId,
      targetCharacterId: CharacterId,
  ) extends GameEvent with ContainsCharacterId
  case class CharacterTeleported(context: GameEventContext, characterId: CharacterId, target: HexCoordinates)
      extends GameEvent with ContainsCharacterId
  case class DamagePrepared(context: GameEventContext, characterId: CharacterId, damage: Damage)
      extends GameEvent
      with ContainsCharacterId
  case class DamageAmplified(context: GameEventContext, damagePreparedId: GameEventId, additionalAmount: Int)
      extends GameEvent
  case class DamageSent(context: GameEventContext, characterId: CharacterId, damage: Damage) extends GameEvent
      with ContainsCharacterId
  case class ShieldDamaged(context: GameEventContext, characterId: CharacterId, damageAmount: Int)
      extends GameEvent
      with ContainsCharacterId
  case class CharacterDamaged(context: GameEventContext, characterId: CharacterId, damageAmount: Int)
      extends GameEvent
      with ContainsCharacterId
  case class HealPrepared(context: GameEventContext, characterId: CharacterId, amount: Int) extends GameEvent
      with ContainsCharacterId
  case class HealAmplified(context: GameEventContext, healPreparedId: GameEventId, additionalAmount: Int)
      extends GameEvent
  case class CharacterHealed(context: GameEventContext, characterId: CharacterId, amount: Int) extends GameEvent
      with ContainsCharacterId
  case class CharacterHpSet(context: GameEventContext, characterId: CharacterId, amount: Int) extends GameEvent
      with ContainsCharacterId
  case class CharacterShieldSet(context: GameEventContext, characterId: CharacterId, amount: Int)
      extends GameEvent
      with ContainsCharacterId
  case class CharacterAttackTypeSet(context: GameEventContext, characterId: CharacterId, attackType: AttackType)
      extends GameEvent with ContainsCharacterId
  case class CharacterStatSet(
      context: GameEventContext,
      characterId: CharacterId,
      statType: StatType,
      amount: Int,
  ) extends GameEvent with ContainsCharacterId
  case class CharacterDied(context: GameEventContext, characterId: CharacterId) extends GameEvent
      with ContainsCharacterId
  case class CharacterRemovedFromMap(context: GameEventContext, characterId: CharacterId) extends GameEvent
      with ContainsCharacterId
  case class CharacterTookAction(context: GameEventContext, characterId: CharacterId) extends GameEvent
      with ContainsCharacterId
  case class BasicAttackRefreshed(context: GameEventContext, characterId: CharacterId) extends GameEvent
      with ContainsCharacterId
  case class BasicMoveRefreshed(context: GameEventContext, characterId: CharacterId) extends GameEvent
      with ContainsCharacterId
  case class AnythingRefreshed(context: GameEventContext, characterId: CharacterId) extends GameEvent
      with ContainsCharacterId
  case class PlayerLost(context: GameEventContext, playerId: PlayerId) extends GameEvent
  case class PlayerWon(context: GameEventContext, playerId: PlayerId) extends GameEvent
  case class PlayerDrew(context: GameEventContext, playerId: PlayerId) extends GameEvent
  case class PlayerSurrendered(context: GameEventContext, playerId: PlayerId) extends GameEvent
  case class PlayerBanned(context: GameEventContext, playerId: PlayerId, characterIds: Set[CharacterMetadataId])
      extends GameEvent
  case class PlayerFinishedBanning(context: GameEventContext, playerId: PlayerId) extends GameEvent
  case class PlayerPicked(context: GameEventContext, playerId: PlayerId, characterId: CharacterMetadataId)
      extends GameEvent
  case class PlayerBlindPicked(context: GameEventContext, playerId: PlayerId, characterIds: Set[CharacterMetadataId])
      extends GameEvent
  case class PlayerFinishedBlindPicking(context: GameEventContext, playerId: PlayerId) extends GameEvent
  case class BanningPhaseFinished(context: GameEventContext) extends GameEvent
  case class PlacingCharactersFinished(context: GameEventContext) extends GameEvent
  case class CharactersPicked(context: GameEventContext) extends GameEvent
  case class TurnFinished(context: GameEventContext, playerId: PlayerId) extends GameEvent
  case class TurnStarted(context: GameEventContext, playerId: PlayerId) extends GameEvent
  case class PhaseFinished(context: GameEventContext) extends GameEvent

  object Ability {
    object MasterThrone {
      case class EnergyCollected(
          context: GameEventContext,
          abilityId: AbilityId,
          fromCharacterId: CharacterId,
          amount: Int,
      ) extends GameEvent
          with ContainsAbilityId
    }
  }
}
