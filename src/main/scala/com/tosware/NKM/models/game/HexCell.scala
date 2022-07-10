package com.tosware.NKM.models.game

import com.tosware.NKM.models.game.HexCellType.Normal
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

object HexCell {
  def empty
  (
    coordinates: HexCoordinates,
    cellType: HexCellType = Normal,
    spawnNumber: Option[Int] = None,
  ): HexCell = HexCell(coordinates, cellType, None, Seq.empty, spawnNumber)
}

case class HexCell
(
  coordinates: HexCoordinates,
  cellType: HexCellType,
  characterId: Option[CharacterId],
  effects: Seq[HexCellEffect],
  spawnNumber: Option[Int],
)
