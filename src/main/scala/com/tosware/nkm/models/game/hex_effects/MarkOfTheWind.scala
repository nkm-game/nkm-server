package com.tosware.nkm.models.game.hex_effects

import com.tosware.nkm.*
import com.tosware.nkm.models.game.hex_effect.*

object MarkOfTheWind {
  val metadata: HexCellEffectMetadata =
    HexCellEffectMetadata(
      name = HexCellEffectName.MarkOfTheWind,
      initialEffectType = HexCellEffectType.Trap,
      description = "Invisible. Deal damage when triggered by `Crack the Sky`.",
    )
}

case class MarkOfTheWind(effectId: HexCellEffectId, initialCooldown: Int)
    extends HexCellEffect(effectId) {
  val metadata: HexCellEffectMetadata = MarkOfTheWind.metadata
}
