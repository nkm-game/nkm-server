package com.tosware.nkm.models.game.hex

import com.tosware.nkm._
import com.tosware.nkm.models.game.hex_effect.HexCellEffectView

case class HexCellView
(
  coordinates: HexCoordinates,
  cellType: HexCellType,
  characterId: Option[CharacterId],
  effects: Seq[HexCellEffectView],
  spawnNumber: Option[Int],
) extends HexCellLike
