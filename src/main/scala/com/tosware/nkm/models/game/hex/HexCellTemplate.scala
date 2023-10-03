package com.tosware.nkm.models.game.hex

case class HexCellTemplate(
    coordinates: HexCoordinates,
    cellType: HexCellType,
    spawnNumber: Option[Int],
) {
  def toCell: HexCell = HexCell.empty(coordinates, cellType, spawnNumber)
}
