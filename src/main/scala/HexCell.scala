abstract class HexCellEffect(cooldown: Int)

sealed trait HexCellType
case object Transparent extends HexCellType
case object Normal extends HexCellType
case object Wall extends HexCellType
case object SpawnPoint extends HexCellType

case class HexCoordinates(x: Int, z: Int)
case class HexCell(coordinates: HexCoordinates,
                   cellType: HexCellType,
                   character: Option[NKMCharacter],
                   effects: Set[HexCellEffect],
                   spawnNumber: Option[Int])
