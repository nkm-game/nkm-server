package com.tosware.nkm.models.game.hex

import com.tosware.nkm.NkmUtils

case class TestHexMap(name: TestHexMapName, params: Set[Any]) {
  val hexMap: HexMap[HexCell] = HexMap(name.toString, NkmUtils.hexCellParamsToCells(params))
}

