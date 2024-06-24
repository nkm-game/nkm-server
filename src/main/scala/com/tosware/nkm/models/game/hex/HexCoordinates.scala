package com.tosware.nkm.models.game.hex

import com.tosware.nkm.models.game.game_state.GameState

import scala.math.*

object HexCoordinates {
  def apply(tuple: (Int, Int)): HexCoordinates = HexCoordinates(tuple._1, tuple._2)
}

case class HexCoordinates(x: Int, z: Int) {
  def y: Int = -x - z
  def getNeighbour(direction: HexDirection): HexCoordinates =
    direction match {
      case HexDirection.NW => HexCoordinates(x - 1, z + 1)
      case HexDirection.W  => HexCoordinates(x - 1, z)
      case HexDirection.SW => HexCoordinates(x, z - 1)
      case HexDirection.SE => HexCoordinates(x + 1, z - 1)
      case HexDirection.E  => HexCoordinates(x + 1, z)
      case HexDirection.NE => HexCoordinates(x, z + 1)
    }

  def getDirection(coordinates: HexCoordinates): Option[HexDirection] = {
    if (coordinates == this) return None
    if (coordinates.x == x) return Some(if (coordinates.y > y) HexDirection.SW else HexDirection.NE)
    if (coordinates.y == y) return Some(if (coordinates.x > x) HexDirection.SE else HexDirection.NW)
    if (coordinates.z == z) return Some(if (coordinates.x > x) HexDirection.E else HexDirection.W)
    None
  }

  // TODO calculate with math
  def getInDirection(hexDirection: HexDirection, distance: Int): HexCoordinates = {
    if (distance <= 0) return this
    getNeighbour(hexDirection).getInDirection(hexDirection, distance - 1)
  }

  def getLine(direction: HexDirection, size: Int): Seq[HexCoordinates] = {
    if (size <= 0) return Seq.empty
    val neighbour = getNeighbour(direction)
    if (size == 1) return Seq(neighbour)
    neighbour +: neighbour.getLine(direction, size - 1)
  }

  def getDistance(target: HexCoordinates): Option[Int] =
    if (this == target) Some(0)
    else getDirection(target).map {
      case HexDirection.NW | HexDirection.SE | HexDirection.W | HexDirection.E => math.abs(x - target.x)
      case HexDirection.NE | HexDirection.SW                                   => math.abs(z - target.z)
    }

  def getThickLine(target: HexCoordinates, width: Int): Seq[HexCoordinates] = (
    for {
      direction <- getDirection(target)
      distance <- getDistance(target)
    } yield getThickLine(direction, width, distance)
  ).getOrElse(Seq.empty)

  def getThickLine(direction: HexDirection, width: Int, length: Int): Seq[HexCoordinates] = {
    if (width <= 0 || length <= 0) return Seq.empty

    val leftLineLines: Seq[Seq[HexCoordinates]] = getLine(direction.lookLeft, width - 1)
      .zipWithIndex
      .map { case (d, i) => d.getLine(direction, length - 1 - i) }

    val rightLineLines: Seq[Seq[HexCoordinates]] = getLine(direction.lookRight, width - 1)
      .zipWithIndex
      .map { case (d, i) => d.getLine(direction, length - 1 - i) }

    val mainLine = getLine(direction, length)

    mainLine ++ leftLineLines.zip(rightLineLines).flatMap { case (a, b) => Seq(a, b).flatten }
  }

  def getLines(directions: Set[HexDirection], size: Int): Set[HexCoordinates] =
    directions.flatMap(d => getLine(d, size))

  def getCircle(size: Int): Set[HexCoordinates] =
    if (size < 0) Set.empty
    else if (size == 0) Set(this)
    else {
      // https://www.redblobgames.com/grids/hexagons/#range-coordinate
      val result = for {
        nx <- -size to size
        nz <- max(-size, -nx - size) to min(size, -nx + size)
      } yield HexCoordinates(x + nx, z + nz)
      result.toSet
    }

  def toTuple: (Int, Int) = (x, z)

  def toCellOpt(implicit gameState: GameState): Option[HexCell] =
    gameState.hexMap.getCellOpt(this)

  def toCell(implicit gameState: GameState): HexCell =
    toCellOpt.get
}
