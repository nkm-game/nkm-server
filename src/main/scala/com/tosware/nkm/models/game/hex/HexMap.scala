package com.tosware.nkm.models.game.hex

import com.tosware.nkm._
import com.tosware.nkm.models.game.GameState

object HexMap {
  def empty: HexMap = HexMap("Empty HexMap", Set.empty)
}

case class HexMap(name: String, cells: Set[HexCell]) extends HexMapLike[HexCell] {
  def toTemplate: HexMapTemplate =
    HexMapTemplate(name, cells.map(_.toTemplate))

  def toView(forPlayerOpt: Option[PlayerId])(implicit gameState: GameState): HexMapView =
    HexMapView(name, cells.map(_.toView(forPlayerOpt)))
}

