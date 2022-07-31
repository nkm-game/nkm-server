package com.tosware.NKM.models.game.hex

import scala.math.abs

object HexUtils {

  // TODO in Scala 3
  // type HexParam = (Int, Int) | (Int, Int, HexCellType) | (Int, Int, HexCellType, Int)

  def hexCellParamsToCells(params: Set[Any]): Set[HexCell] = {
    params.map {
      case (x: Int, y: Int) => HexCell.empty(HexCoordinates(x, y))
      case (x: Int, y: Int, t: HexCellType) => HexCell.empty(HexCoordinates(x, y), t)
      case (x: Int, y: Int, t: HexCellType, i: Int) => HexCell.empty(HexCoordinates(x, y), t, Some(i))
    }
  }

  def getAdjacentCells(cells: Set[HexCell], targetCellCoordinates: HexCoordinates): Set[HexCell] = {
    cells.filter(c =>
      abs(c.coordinates.x - targetCellCoordinates.x) <= 1
        && abs(c.coordinates.y - targetCellCoordinates.y) <= 1
        && abs(c.coordinates.z - targetCellCoordinates.z) <= 1
    ).filter(c => c.coordinates != targetCellCoordinates)
  }

  def CoordinateSeq(tuples: (Int, Int)*): Seq[HexCoordinates] = tuples.map{case (x, z) => HexCoordinates(x, z)}
  def CoordinateSet(tuples: (Int, Int)*): Set[HexCoordinates] = CoordinateSeq(tuples: _*).toSet
}
