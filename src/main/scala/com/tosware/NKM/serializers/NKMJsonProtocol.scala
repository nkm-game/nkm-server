package com.tosware.NKM.serializers

import com.tosware.NKM._
import com.tosware.NKM.actors.Game.{CharacterMoved, CharacterPlaced, Event}
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
        case _ => deserializationError(s"Unable to parse:\n$value")
      }
      case _ => deserializationError(s"Unable to parse:\n$json")
    }
  }

  implicit val hexCoordinatesFormat: RootJsonFormat[HexCoordinates] = jsonFormat2(HexCoordinates)
  implicit val statFormat: RootJsonFormat[Stat] = jsonFormat1(Stat)
  implicit val nkmCharacterFormat: RootJsonFormat[NKMCharacter] = jsonFormat7(NKMCharacter)
  implicit val hexCellFormat: RootJsonFormat[HexCell] = jsonFormat5(HexCell)
  implicit val hexMapFormat: RootJsonFormat[HexMap] = jsonFormat2(HexMap)
  implicit val gameStateFormat: RootJsonFormat[GameState] = jsonFormat2(GameState)

  // Events
  implicit val characterPlacedFormat: RootJsonFormat[CharacterPlaced] = jsonFormat2(CharacterPlaced)
  implicit val characterMovedFormat: RootJsonFormat[CharacterMoved] = jsonFormat2(CharacterMoved)

  implicit object EventJsonFormat extends RootJsonFormat[Event] {
    val characterPlacedId: JsString = JsString("CharacterPlaced")
    val characterMovedId: JsString = JsString("CharacterMoved")

    override def write(obj: Event): JsValue = obj match {
      case e: CharacterPlaced => JsArray(Vector(characterPlacedId, characterPlacedFormat.write(e)))
      case e: CharacterMoved => JsArray(Vector(characterMovedId, characterMovedFormat.write(e)))
    }
    override def read(json: JsValue): Event = json match {
      case JsArray(Vector(`characterPlacedId`, jsEvent)) => characterPlacedFormat.read(jsEvent)
      case JsArray(Vector(`characterMovedId`, jsEvent)) => characterMovedFormat.read(jsEvent)
    }
  }


}
