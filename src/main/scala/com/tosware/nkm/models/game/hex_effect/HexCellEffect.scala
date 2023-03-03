package com.tosware.nkm.models.game.hex_effect

import com.softwaremill.quicklens._
import com.tosware.nkm.NkmUtils
import com.tosware.nkm.models.game.GameState
import com.tosware.nkm.models.game.hex.HexCell
import com.tosware.nkm.models.game.hex_effect.HexCellEffect._
object HexCellEffect {
  type HexCellEffectId = String
  type HexCellEffectMetadataId = String
}

abstract class HexCellEffect(val id: HexCellEffectId) extends NkmUtils {
  val metadata: HexCellEffectMetadata
  val initialCooldown: Int

  def effectType(implicit gameState: GameState): HexCellEffectType =
    metadata.initialEffectType
  def state(implicit gameState: GameState): HexCellEffectState =
    gameState.hexCellEffectStates(id)
  def parentCell(implicit gameState: GameState): HexCell =
    gameState.hexMap.cells.find(_.effects.exists(_.id == id)).get

  def getDecrementCooldownState(implicit gameState: GameState): HexCellEffectState =
    state.copy(cooldown = math.max(state.cooldown - 1, 0))

  def getVariablesChangedState(key: String, value: String)(implicit gameState: GameState): HexCellEffectState =
    state.modify(_.variables).using(_.updated(key, value))

  def toView(implicit gameState: GameState): HexCellEffectView =
    HexCellEffectView(
      id = id,
      metadataId = metadata.id,
      parentCellCoordinates = parentCell.coordinates,
      state = state,
      effectType = effectType,
    )
}
