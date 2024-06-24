package com.tosware.nkm.models.game.hex

import com.tosware.nkm.*

case class TestHexMap(name: TestHexMapName, cellParams: Set[Any], pointParams: Seq[Set[(Int, Int)]] = Seq.empty) {
  val hexMap: HexMap = HexMap(name.toString, hexCellParamsToCells(cellParams), pointParamsToPointGroups(pointParams))
}
