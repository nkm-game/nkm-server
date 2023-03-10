package com.tosware.nkm.models.game.hex_effect

import com.tosware.nkm._

case class HexCellEffectView
(
  id: HexCellEffectId,
  metadataId: HexCellEffectMetadataId,
  state: HexCellEffectState,
  effectType: HexCellEffectType,
)
