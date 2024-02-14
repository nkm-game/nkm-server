package com.tosware.nkm.models.game.character_effect

import com.softwaremill.quicklens.*
import com.tosware.nkm.*
import com.tosware.nkm.models.game.character.NkmCharacter
import com.tosware.nkm.models.game.event.{GameEvent, GameEventListener}
import com.tosware.nkm.models.game.game_state.GameState
import com.tosware.nkm.models.game.hex.HexCell
import com.tosware.nkm.serializers.NkmJsonProtocol

import scala.util.Random

abstract class CharacterEffect(val id: CharacterEffectId)
    extends NkmJsonProtocol
    with GameEventListener {
  val metadata: CharacterEffectMetadata
  val initialCooldown: Int

  def description(implicit gameState: GameState): String

  final override def onEvent(e: GameEvent.GameEvent)(implicit random: Random, gameState: GameState): GameState =
    e match {
      case GameEvent.EffectAddedToCharacter(_, _, eid, _) if id == eid =>
        onInit()
      case _ => onEventReceived(e)
    }

  def onInit()(implicit random: Random, gameState: GameState): GameState =
    gameState
  def onEventReceived(e: GameEvent.GameEvent)(implicit random: Random, gameState: GameState): GameState =
    gameState
  def effectType(implicit gameState: GameState): CharacterEffectType =
    metadata.initialEffectType
  def state(implicit gameState: GameState): CharacterEffectState =
    gameState.characterEffectStates(id)
  def parentCharacter(implicit gameState: GameState): NkmCharacter =
    gameState.characters.find(_.state.effects.map(_.id).contains(id)).get
  def parentCell(implicit gameState: GameState): Option[HexCell] =
    parentCharacter.parentCellOpt
  def getDecrementCooldownState(implicit gameState: GameState): CharacterEffectState =
    state.copy(cooldown = math.max(state.cooldown - 1, 0))
  def getVariablesChangedState(key: String, value: String)(implicit gameState: GameState): CharacterEffectState =
    state.modify(_.variables).using(_.updated(key, value))
  def toView(forPlayerOpt: Option[PlayerId])(implicit gameState: GameState): Option[CharacterEffectView] =
    if (parentCharacter.isSeenBy(forPlayerOpt))
      Some(CharacterEffectView(
        id = id,
        metadataId = metadata.id,
        parentCharacterId = parentCharacter.id,
        state = state,
        effectType = effectType,
        description = description,
      ))
    else None
}
