abstract class HexEffect(cooldown: Int)

case class HexCoordinates(x: Int, y: Int)
case class HexCell(coordinates: HexCoordinates, character: Option[NKMCharacter], effects: Set[HexEffect])
