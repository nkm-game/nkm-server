package com.tosware.nkm.models.game

import com.tosware.nkm.models.Damage
import com.tosware.nkm.models.game.Ability.AbilityId
import com.tosware.nkm.models.game.CharacterEffect.CharacterEffectId
import com.tosware.nkm.models.game.NkmCharacter.CharacterId
import com.tosware.nkm.models.game.Player.PlayerId
import com.tosware.nkm.models.game.hex.HexCoordinates

object GameEvent {
  type GameEventId = String
  sealed trait GameEvent
  {
    val id: GameEventId
    implicit val phase: Phase
    implicit val turn: Turn
    implicit val causedById: String
    val hiddenFor: Set[Option[PlayerId]] = Set.empty
    def index(implicit gameState: GameState): Int =
      gameState.gameLog.events.indexWhere(_.id == id)
  }
  trait ContainsCharacterId {
    val characterId: CharacterId
  }
  trait ContainsAbilityId {
    val abilityId: AbilityId
  }

  case class ClockUpdated(id: GameEventId, phase: Phase, turn: Turn, causedById: String, newClock: Clock) extends GameEvent
  case class CharacterPlaced(id: GameEventId, phase: Phase, turn: Turn, causedById: String, characterId: CharacterId, target: HexCoordinates)
                             extends GameEvent
    with ContainsCharacterId
  case class EffectAddedToCell(id: GameEventId, phase: Phase, turn: Turn, causedById: String, effectId: String, target: HexCoordinates)
                               extends GameEvent
  case class EffectAddedToCharacter(id: GameEventId, phase: Phase, turn: Turn, causedById: String, effectId: CharacterEffectId, characterId: CharacterId)
                                    extends GameEvent
  case class EffectRemovedFromCharacter(id: GameEventId, phase: Phase, turn: Turn, causedById: String, effectId: CharacterEffectId)
                                        extends GameEvent
  case class AbilityHitCharacter(id: GameEventId, phase: Phase, turn: Turn, causedById: String, abilityId: AbilityId, targetCharacterId: CharacterId)
                                 extends GameEvent
    with ContainsAbilityId
  case class AbilityUsedWithoutTarget(id: GameEventId, phase: Phase, turn: Turn, causedById: String, abilityId: AbilityId)
                                      extends GameEvent
    with ContainsAbilityId
  case class AbilityUsedOnCoordinates(id: GameEventId, phase: Phase, turn: Turn, causedById: String, abilityId: AbilityId, target: HexCoordinates)
                                      extends GameEvent
    with ContainsAbilityId
  case class AbilityUsedOnCharacter(id: GameEventId, phase: Phase, turn: Turn, causedById: String, abilityId: AbilityId, targetCharacterId: CharacterId)
                                    extends GameEvent
    with ContainsAbilityId
  case class AbilityUseFinished(id: GameEventId, phase: Phase, turn: Turn, causedById: String, abilityId: AbilityId)
     extends GameEvent
    with ContainsAbilityId
  case class AbilityVariableSet(id: GameEventId, phase: Phase, turn: Turn, causedById: String, abilityId: AbilityId, key: String, value: String)
     extends GameEvent
    with ContainsAbilityId
  case class CharacterBasicMoved(id: GameEventId, phase: Phase, turn: Turn, causedById: String, characterId: CharacterId, path: Seq[HexCoordinates])
                                 extends GameEvent
    with ContainsCharacterId
  case class CharacterPreparedToAttack(id: GameEventId, phase: Phase, turn: Turn, causedById: String, characterId: CharacterId, targetCharacterId: CharacterId)
                                    extends GameEvent
    with ContainsCharacterId
  case class CharacterBasicAttacked(id: GameEventId, phase: Phase, turn: Turn, causedById: String, characterId: CharacterId, targetCharacterId: CharacterId)
                                    extends GameEvent
    with ContainsCharacterId
  case class CharacterTeleported(id: GameEventId, phase: Phase, turn: Turn, causedById: String, characterId: CharacterId, target: HexCoordinates)
                                 extends GameEvent
    with ContainsCharacterId
  case class CharacterDamaged(id: GameEventId, phase: Phase, turn: Turn, causedById: String, characterId: CharacterId, damage: Damage)
                              extends GameEvent
    with ContainsCharacterId
  case class CharacterHealed(id: GameEventId, phase: Phase, turn: Turn, causedById: String, characterId: CharacterId, amount: Int)
                             extends GameEvent
    with ContainsCharacterId
  case class CharacterHpSet(id: GameEventId, phase: Phase, turn: Turn, causedById: String, characterId: CharacterId, amount: Int)
                            extends GameEvent
    with ContainsCharacterId
  case class CharacterShieldSet(id: GameEventId, phase: Phase, turn: Turn, causedById: String, characterId: CharacterId, amount: Int)
                            extends GameEvent
    with ContainsCharacterId
  case class CharacterStatSet(id: GameEventId, phase: Phase, turn: Turn, causedById: String, characterId: CharacterId, statType: StatType, amount: Int)
                              extends GameEvent
    with ContainsCharacterId
  case class CharacterDied(id: GameEventId, phase: Phase, turn: Turn, causedById: String, characterId: CharacterId)
                           extends GameEvent
    with ContainsCharacterId
  case class CharacterRemovedFromMap(id: GameEventId, phase: Phase, turn: Turn, causedById: String, characterId: CharacterId)
                                     extends GameEvent
    with ContainsCharacterId
  case class CharacterTookAction(id: GameEventId, phase: Phase, turn: Turn, causedById: String, characterId: CharacterId)
                                 extends GameEvent
    with ContainsCharacterId
  case class BasicAttackRefreshed(id: GameEventId, phase: Phase, turn: Turn, causedById: String, characterId: CharacterId)
                                  extends GameEvent
    with ContainsCharacterId
  case class BasicMoveRefreshed(id: GameEventId, phase: Phase, turn: Turn, causedById: String, characterId: CharacterId)
                                extends GameEvent
    with ContainsCharacterId
  case class CharactersPicked(id: GameEventId, phase: Phase, turn: Turn, causedById: String)
                              extends GameEvent
  case class TurnFinished(id: GameEventId, phase: Phase, turn: Turn, causedById: String)
                          extends GameEvent
  case class TurnStarted(id: GameEventId, phase: Phase, turn: Turn, causedById: String)
                         extends GameEvent
  case class PhaseFinished(id: GameEventId, phase: Phase, turn: Turn, causedById: String)
                           extends GameEvent
}
