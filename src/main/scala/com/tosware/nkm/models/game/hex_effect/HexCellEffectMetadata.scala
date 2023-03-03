package com.tosware.nkm.models.game.hex_effect

import com.tosware.nkm.models.game.hex_effect.HexCellEffect.HexCellEffectId

case class HexCellEffectMetadata
(
  name: HexCellEffectName,
  initialEffectType: HexCellEffectType,
  description: String,
) {
  def id: HexCellEffectId = name.toString
}
