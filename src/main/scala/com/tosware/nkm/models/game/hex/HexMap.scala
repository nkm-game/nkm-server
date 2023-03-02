package com.tosware.nkm.models.game.hex

object HexMap {
  def empty[T <: HexCellLike]: HexMap[T] = HexMap("Empty HexMap", Set.empty)
}

case class HexMap[T <: HexCellLike](name: String, cells: Set[T]) extends HexMapLike[T]

