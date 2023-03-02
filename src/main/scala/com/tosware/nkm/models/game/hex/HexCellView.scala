package com.tosware.nkm.models.game.hex

import com.tosware.nkm.models.game.character.NkmCharacter.CharacterId

case class HexCellView
(
  coordinates: HexCoordinates,
  cellType: HexCellType,
  characterId: Option[CharacterId],
  effects: Seq[HexCellEffect],
  spawnNumber: Option[Int],
) extends HexCellLike
