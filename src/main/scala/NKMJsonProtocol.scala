import spray.json._

trait NKMJsonProtocol extends DefaultJsonProtocol {

  implicit object HexEffectJsonFormat extends RootJsonFormat[HexEffect] {
    override def write(obj: HexEffect): JsValue = obj match {
      case _ => deserializationError("Not implemented yet")
    }
    override def read(json: JsValue): HexEffect = json match {
      case _ => deserializationError("Cannot deserialize abstract class")
    }
  }

  implicit val hexCoordinatesFormat: RootJsonFormat[HexCoordinates] = jsonFormat2(HexCoordinates)
  implicit val statFormat: RootJsonFormat[Stat] = jsonFormat1(Stat)
  implicit val nkmCharacterFormat: RootJsonFormat[NKMCharacter] = jsonFormat7(NKMCharacter)
  implicit val hexCellFormat: RootJsonFormat[HexCell] = jsonFormat3(HexCell.apply)
  implicit val hexMapFormat: RootJsonFormat[HexMap] = jsonFormat1(HexMap)
  implicit val gameStateFormat: RootJsonFormat[GameState] = jsonFormat2(GameState)
}
