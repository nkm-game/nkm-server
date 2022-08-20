package com.tosware.NKM.models.game.hex
import scala.math._

case class HexCoordinates(x: Int, z: Int) {
  def y: Int = -x - z
  def getNeighbour(direction: HexDirection): HexCoordinates = {
    direction match {
      case HexDirection.NW => HexCoordinates(x-1, z+1)
      case HexDirection.W => HexCoordinates(x-1, z)
      case HexDirection.SW => HexCoordinates(x, z-1)
      case HexDirection.SE => HexCoordinates(x+1, z-1)
      case HexDirection.E => HexCoordinates(x+1, z)
      case HexDirection.NE => HexCoordinates(x, z+1)
    }
  }

  def getInDirection(hexDirection: HexDirection, distance: Int): HexCoordinates = {
    if(distance <= 0) return this
    getNeighbour(hexDirection).getInDirection(hexDirection, distance - 1)
  }

  def getLine(direction: HexDirection, size: Int): Set[HexCoordinates] = {
    if(size <= 0) return Set.empty
    val neighbour = getNeighbour(direction)
    if(size == 1) return Set(neighbour)
    neighbour.getLine(direction, size - 1) + neighbour
  }

  def getLines(directions: Set[HexDirection], size: Int): Set[HexCoordinates] =
    directions.flatMap(d => getLine(d, size))

  def getCircle(size: Int): Set[HexCoordinates] =
    if(size < 0) Set.empty
    else if(size == 0) Set(this)
    else {
      // https: //www.redblobgames.com/grids/hexagons/#range-coordinate
      val result = for {
        x <- -size to size
        z <- max(-size, -x-size) to min(size, -x+size)
      } yield HexCoordinates(x, z)
      result.toSet
    }

  def toTuple: (Int, Int) = (x, z)

  def toCellOpt(implicit hexMap: HexMap): Option[HexCell] =
    hexMap.getCell(this)

  def toCell(implicit hexMap: HexMap): HexCell =
    toCellOpt.get


}

