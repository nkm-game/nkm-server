package com.tosware.nkm.models.game.hex

case class HexMapView(name: String, cells: Set[HexCell]) extends HexMapLike
