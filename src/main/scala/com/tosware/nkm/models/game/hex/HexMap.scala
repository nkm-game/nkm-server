package com.tosware.nkm.models.game.hex

import com.tosware.nkm.*
import com.tosware.nkm.models.game.game_state.GameState

object HexMap {
  def empty: HexMap = HexMap("Empty HexMap", Set.empty)
}

case class HexMap(name: String, cells: Set[HexCell], pointGroups: Seq[HexPointGroup] = Seq.empty)
    extends HexMapLike[HexCell] {
  def toTemplate: HexMapTemplate =
    HexMapTemplate(name, cells.map(_.toTemplate), Some(pointGroups))

  def toView(forPlayerOpt: Option[PlayerId])(implicit gameState: GameState): HexMapView =
    HexMapView(name, cells.map(_.toView(forPlayerOpt)), pointGroups)
}
