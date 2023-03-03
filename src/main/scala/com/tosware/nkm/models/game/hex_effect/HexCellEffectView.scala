package com.tosware.nkm.models.game.hex_effect

import com.tosware.nkm.models.game.hex.HexCoordinates
import com.tosware.nkm.models.game.hex_effect.HexCellEffect._

case class HexCellEffectView
(
  id: HexCellEffectId,
  metadataId: HexCellEffectMetadataId,
  parentCellCoordinates: HexCoordinates,
  state: HexCellEffectState,
  effectType: HexCellEffectType,
)
