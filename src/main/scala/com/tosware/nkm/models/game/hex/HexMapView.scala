package com.tosware.nkm.models.game.hex

case class HexMapView(name: String, cells: Set[HexCellView]) extends HexMapLike[HexCellView]