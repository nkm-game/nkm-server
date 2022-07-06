package com.tosware.NKM.models.game

import com.tosware.NKM.models.game.NKMCharacter.CharacterId
import enumeratum._

case class HexCellEffect(cooldown: Int)

sealed trait HexCellType extends EnumEntry
object HexCellType extends Enum[HexCellType] {
  val values = findValues

  case object Transparent extends HexCellType
  case object Normal extends HexCellType
  case object Wall extends HexCellType
  case object SpawnPoint extends HexCellType
}

case class HexCoordinates(x: Int, z: Int)

case class HexCell(coordinates: HexCoordinates,
                   cellType: HexCellType,
                   characterId: Option[CharacterId],
                   effects: List[HexCellEffect],
                   spawnNumber: Option[Int])
