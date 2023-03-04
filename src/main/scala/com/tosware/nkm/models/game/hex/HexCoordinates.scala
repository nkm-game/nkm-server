package com.tosware.nkm.models.game.hex
import com.tosware.nkm.models.game.GameState

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

  def getDirection(coordinates: HexCoordinates): Option[HexDirection] = {
    if(coordinates == this) return None
    if(coordinates.x == x) return Some(if(coordinates.y > y) HexDirection.SW else HexDirection.NE)
    if(coordinates.y == y) return Some(if(coordinates.x > x) HexDirection.SE else HexDirection.NW)
    if(coordinates.z == z) return Some(if(coordinates.x > x) HexDirection.E else HexDirection.W)
    None
  }

  def getInDirection(hexDirection: HexDirection, distance: Int): HexCoordinates = {
    if(distance <= 0) return this
    getNeighbour(hexDirection).getInDirection(hexDirection, distance - 1)
  }

  def getLine(direction: HexDirection, size: Int): Seq[HexCoordinates] = {
    if(size <= 0) return Seq.empty
    val neighbour = getNeighbour(direction)
    if(size == 1) return Seq(neighbour)
    neighbour +: neighbour.getLine(direction, size - 1)
  }

  def getLines(directions: Set[HexDirection], size: Int): Set[HexCoordinates] =
    directions.flatMap(d => getLine(d, size))

  def getCircle(size: Int): Set[HexCoordinates] =
    if(size < 0) Set.empty
    else if(size == 0) Set(this)
    else {
      // https://www.redblobgames.com/grids/hexagons/#range-coordinate
      val result = for {
        nx <- -size to size
        nz <- max(-size, -nx-size) to min(size, -nx+size)
      } yield HexCoordinates(x + nx, z + nz)
      result.toSet
    }

  def toTuple: (Int, Int) = (x, z)

  def toCellOpt(implicit gameState: GameState): Option[HexCell] =
    gameState.hexMap.getCell(this)

  def toCell(implicit gameState: GameState): HexCell =
    toCellOpt.get
}

