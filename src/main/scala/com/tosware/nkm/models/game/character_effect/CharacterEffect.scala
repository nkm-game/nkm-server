package com.tosware.nkm.models.game.character_effect

import com.softwaremill.quicklens._
import com.tosware.nkm._
import com.tosware.nkm.models.game.GameState
import com.tosware.nkm.models.game.character.NkmCharacter
import com.tosware.nkm.models.game.hex.HexCell
import com.tosware.nkm.serializers.NkmJsonProtocol

abstract class CharacterEffect(val id: CharacterEffectId)
  extends NkmJsonProtocol
{
  val metadata: CharacterEffectMetadata
  val initialCooldown: Int

  def effectType(implicit gameState: GameState): CharacterEffectType =
    metadata.initialEffectType
  def state(implicit gameState: GameState): CharacterEffectState =
    gameState.characterEffectStates(id)
  def parentCharacter(implicit gameState: GameState): NkmCharacter =
    gameState.characters.find(_.state.effects.map(_.id).contains(id)).get
  def parentCell(implicit gameState: GameState): Option[HexCell] =
    parentCharacter.parentCell

  def getDecrementCooldownState(implicit gameState: GameState): CharacterEffectState =
    state.copy(cooldown = math.max(state.cooldown - 1, 0))

  def getVariablesChangedState(key: String, value: String)(implicit gameState: GameState): CharacterEffectState =
    state.modify(_.variables).using(_.updated(key, value))

  def toView(implicit gameState: GameState): CharacterEffectView =
    CharacterEffectView(
      id = id,
      metadataId = metadata.id,
      parentCharacterId = parentCharacter.id,
      state = state,
      effectType = effectType,
    )
}
