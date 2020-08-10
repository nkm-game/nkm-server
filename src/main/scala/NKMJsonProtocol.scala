import spray.json._

trait NKMJsonProtocol extends DefaultJsonProtocol {

  implicit object HexCellEffectJsonFormat extends RootJsonFormat[HexCellEffect] {
    override def write(obj: HexCellEffect): JsValue = obj match {
      case _ => deserializationError("Not implemented yet")
    }
    override def read(json: JsValue): HexCellEffect = json match {
      case _ => deserializationError("Cannot deserialize abstract class")
    }
  }

  implicit object HexCellTypeJsonFormat extends RootJsonFormat[HexCellType] {

    override def write(obj: HexCellType): JsValue = obj match {
      case Transparent => JsString("Transparent")
      case Normal => JsString("Normal")
      case Wall => JsString("Wall")
      case SpawnPoint => JsString("SpawnPoint")
    }
    override def read(json: JsValue): HexCellType = json match {
      case JsString(value) => value match {
        case "Transparent" => Transparent
        case "Normal" => Normal
        case "Wall" => Wall
        case "SpawnPoint" => SpawnPoint
      }
    }
  }

  implicit val hexCoordinatesFormat: RootJsonFormat[HexCoordinates] = jsonFormat2(HexCoordinates)
  implicit val statFormat: RootJsonFormat[Stat] = jsonFormat1(Stat)
//  val NKMCharacterApply: (String, Int, Int, Int, Int, Int, Int) => NKMCharacter = NKMCharacter.apply
  implicit val nkmCharacterFormat: RootJsonFormat[NKMCharacter] = jsonFormat7(NKMCharacter)
  implicit val hexCellFormat: RootJsonFormat[HexCell] = jsonFormat4(HexCell)
  implicit val hexMapFormat: RootJsonFormat[HexMap] = jsonFormat1(HexMap)
  implicit val gameStateFormat: RootJsonFormat[GameState] = jsonFormat2(GameState)
}
