package com.tosware.nkm.models.game

import com.tosware.nkm.models.Damage
import com.tosware.nkm.models.game.Ability.AbilityId
import com.tosware.nkm.models.game.CharacterEffect.CharacterEffectId
import com.tosware.nkm.models.game.NkmCharacter.CharacterId
import com.tosware.nkm.models.game.hex.HexCoordinates

object GameEvent {
  type GameEventId = String
  abstract class GameEvent(val eid: GameEventId)(implicit val phase: Phase, val turn: Turn, val causedById: String) {
    def index(implicit gameState: GameState): Int =
      gameState.gameLog.events.indexWhere(_.eid == eid)
  }
  trait ContainsCharacterId {
    val characterId: CharacterId
  }
  trait ContainsAbilityId {
    val abilityId: AbilityId
  }

  case class ClockUpdated(id: GameEventId, newClock: Clock)
                         (implicit phase: Phase, turn: Turn, causedById: String) extends GameEvent(id)
  case class CharacterPlaced(id: GameEventId, characterId: CharacterId, target: HexCoordinates)
                            (implicit phase: Phase, turn: Turn, causedById: String) extends GameEvent(id)
    with ContainsCharacterId
  case class EffectAddedToCell(id: GameEventId, effectId: String, target: HexCoordinates)
                              (implicit phase: Phase, turn: Turn, causedById: String) extends GameEvent(id)
  case class EffectAddedToCharacter(id: GameEventId, effectId: CharacterEffectId, characterId: CharacterId)
                                   (implicit phase: Phase, turn: Turn, causedById: String) extends GameEvent(id)
  case class EffectRemovedFromCharacter(id: GameEventId, effectId: CharacterEffectId)
                                       (implicit phase: Phase, turn: Turn, causedById: String) extends GameEvent(id)
  case class AbilityHitCharacter(id: GameEventId, abilityId: AbilityId, targetCharacterId: CharacterId)
                                (implicit phase: Phase, turn: Turn, causedById: String) extends GameEvent(id)
    with ContainsAbilityId
  case class AbilityUsedWithoutTarget(id: GameEventId, abilityId: AbilityId)
                                     (implicit phase: Phase, turn: Turn, causedById: String) extends GameEvent(id)
    with ContainsAbilityId
  case class AbilityUsedOnCoordinates(id: GameEventId, abilityId: AbilityId, target: HexCoordinates)
                                     (implicit phase: Phase, turn: Turn, causedById: String) extends GameEvent(id)
    with ContainsAbilityId
  case class AbilityUsedOnCharacter(id: GameEventId, abilityId: AbilityId, targetCharacterId: CharacterId)
                                   (implicit phase: Phase, turn: Turn, causedById: String) extends GameEvent(id)
    with ContainsAbilityId
  case class CharacterBasicMoved(id: GameEventId, characterId: CharacterId, path: Seq[HexCoordinates])
                                (implicit phase: Phase, turn: Turn, causedById: String) extends GameEvent(id)
    with ContainsCharacterId
  case class CharacterPreparedToAttack(id: GameEventId, characterId: CharacterId, targetCharacterId: CharacterId)
                                   (implicit phase: Phase, turn: Turn, causedById: String) extends GameEvent(id)
    with ContainsCharacterId
  case class CharacterBasicAttacked(id: GameEventId, characterId: CharacterId, targetCharacterId: CharacterId)
                                   (implicit phase: Phase, turn: Turn, causedById: String) extends GameEvent(id)
    with ContainsCharacterId
  case class CharacterTeleported(id: GameEventId, characterId: CharacterId, target: HexCoordinates)
                                (implicit phase: Phase, turn: Turn, causedById: String) extends GameEvent(id)
    with ContainsCharacterId
  case class CharacterDamaged(id: GameEventId, characterId: CharacterId, damage: Damage)
                             (implicit phase: Phase, turn: Turn, causedById: String) extends GameEvent(id)
    with ContainsCharacterId
  case class CharacterHealed(id: GameEventId, characterId: CharacterId, amount: Int)
                            (implicit phase: Phase, turn: Turn, causedById: String) extends GameEvent(id)
    with ContainsCharacterId
  case class CharacterHpSet(id: GameEventId, characterId: CharacterId, amount: Int)
                           (implicit phase: Phase, turn: Turn, causedById: String) extends GameEvent(id)
    with ContainsCharacterId
  case class CharacterShieldSet(id: GameEventId, characterId: CharacterId, amount: Int)
                           (implicit phase: Phase, turn: Turn, causedById: String) extends GameEvent(id)
    with ContainsCharacterId
  case class CharacterStatSet(id: GameEventId, characterId: CharacterId, statType: StatType, amount: Int)
                             (implicit phase: Phase, turn: Turn, causedById: String) extends GameEvent(id)
    with ContainsCharacterId
  case class CharacterDied(id: GameEventId, characterId: CharacterId)
                          (implicit phase: Phase, turn: Turn, causedById: String) extends GameEvent(id)
    with ContainsCharacterId
  case class CharacterRemovedFromMap(id: GameEventId, characterId: CharacterId)
                                    (implicit phase: Phase, turn: Turn, causedById: String) extends GameEvent(id)
    with ContainsCharacterId
  case class CharacterTookAction(id: GameEventId, characterId: CharacterId)
                                (implicit phase: Phase, turn: Turn, causedById: String) extends GameEvent(id)
    with ContainsCharacterId
  case class BasicAttackRefreshed(id: GameEventId, characterId: CharacterId)
                                 (implicit phase: Phase, turn: Turn, causedById: String) extends GameEvent(id)
    with ContainsCharacterId
  case class BasicMoveRefreshed(id: GameEventId, characterId: CharacterId)
                               (implicit phase: Phase, turn: Turn, causedById: String) extends GameEvent(id)
    with ContainsCharacterId
  case class CharactersPicked(id: GameEventId)
                             (implicit phase: Phase, turn: Turn, causedById: String) extends GameEvent(id)
  case class TurnFinished(id: GameEventId)
                         (implicit phase: Phase, turn: Turn, causedById: String) extends GameEvent(id)
  case class TurnStarted(id: GameEventId)
                        (implicit phase: Phase, turn: Turn, causedById: String) extends GameEvent(id)
  case class PhaseFinished(id: GameEventId)
                          (implicit phase: Phase, turn: Turn, causedById: String) extends GameEvent(id)
}
