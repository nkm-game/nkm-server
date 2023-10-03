package com.tosware.nkm.models.game.hex

object HexMapTemplate {
  def empty: HexMapTemplate = HexMapTemplate("Empty HexMap", Set.empty)
}

case class HexMapTemplate(name: String, cellTemplates: Set[HexCellTemplate]) {
  def toHexMap: HexMap =
    HexMap(name, cellTemplates.map(_.toCell))
}
