package com.tosware.nkm.models.game.hex

import com.tosware.nkm.HexPointGroupId

case class HexPointGroup(coordinates: Set[HexCoordinates], pointsPerPhase: Int) {
  val id: HexPointGroupId = coordinates.toSeq.map(_.toString).sorted.mkString("-")
}
