package com.tosware.NKM.models

abstract class HexCellEffect(cooldown: Int)

sealed trait HexCellType
case object Transparent extends HexCellType
case object Normal extends HexCellType
case object Wall extends HexCellType
case object SpawnPoint extends HexCellType

case class HexCoordinates(x: Int, z: Int)
case class HexCell(coordinates: HexCoordinates,
                   cellType: HexCellType,
                   characterId: Option[String],
                   effects: List[HexCellEffect],
                   spawnNumber: Option[Int])
