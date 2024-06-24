package com.tosware.nkm.models.game.hex

object HexMapTemplate {
  def empty: HexMapTemplate = HexMapTemplate("Empty HexMap", Set.empty, None)
}

case class HexMapTemplate(
    name: String,
    cellTemplates: Set[HexCellTemplate],
    pointGroups: Option[Seq[HexPointGroup]] = None,
) {
  def toHexMap: HexMap =
    HexMap(name, cellTemplates.map(_.toCell), pointGroups.getOrElse(Seq.empty))
}
