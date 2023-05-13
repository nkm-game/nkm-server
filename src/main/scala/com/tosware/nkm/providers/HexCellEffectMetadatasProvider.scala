package com.tosware.nkm.providers

import com.tosware.nkm.models.game.hex_effects.*
import com.tosware.nkm.models.game.hex_effect.HexCellEffectMetadata
import com.tosware.nkm.serializers.NkmJsonProtocol

case class HexCellEffectMetadatasProvider() extends NkmJsonProtocol {
  def getHexCellEffectMetadatas: Seq[HexCellEffectMetadata] = Seq(
    MarkOfTheWind.metadata,
  )
}
