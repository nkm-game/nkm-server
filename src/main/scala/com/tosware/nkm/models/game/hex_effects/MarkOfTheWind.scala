package com.tosware.nkm.models.game.hex_effects

import com.tosware.nkm._
import com.tosware.nkm.models.game.hex_effect._

object MarkOfTheWind {
  val metadata: HexCellEffectMetadata =
    HexCellEffectMetadata(
      name = HexCellEffectName.MarkOfTheWind,
      initialEffectType = HexCellEffectType.Trap,
      description = "Invisible. Deals damage when triggered by Crack the Sky ability.",
    )
}

case class MarkOfTheWind(effectId: HexCellEffectId, initialCooldown: Int)
  extends HexCellEffect(effectId)
{
  val metadata: HexCellEffectMetadata = MarkOfTheWind.metadata
}
