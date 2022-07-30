package com.tosware.NKM.models.game.hex

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
  def getLine(direction: HexDirection, size: Int): Set[HexCoordinates] = {
    if(size <= 0) return Set.empty
    val neighbour = getNeighbour(direction)
    if(size == 1) return Set(neighbour)
    neighbour.getLine(direction, size - 1) + neighbour
  }

  def getLines(directions: Set[HexDirection], size: Int): Set[HexCoordinates] =
    directions.flatMap(d => getLine(d, size))

}

