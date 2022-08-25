package com.tosware.nkm.serializers

import com.tosware.nkm.models.game.NkmCharacter.CharacterId
import com.tosware.nkm.models.game.hex.HexCoordinates
import pl.iterators.kebs.json.{KebsEnumFormats, KebsSpray}
import spray.json._

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.util.Try

trait NkmJsonProtocol
  extends DefaultJsonProtocol
    with KebsSpray
    with KebsEnumFormats // enumeratum support
    with KebsSpray.NoFlat // jwt serialize / deserialize does not work with flat serialization (idk why)
{

  // borrowed from https://github.com/theiterators/kebs/blob/master/examples/src/main/scala/pl/iterators/kebs_examples/SprayJsonWithAkkaHttpExample.scala
  implicit object LocalDateTimeFormat extends RootJsonFormat[LocalDateTime] {
    override def write(obj: LocalDateTime) = JsString(formatter.format(obj))

    override def read(json: JsValue) = json match {
      case JsString(lDString) =>
        Try(LocalDateTime.parse(lDString, formatter)).getOrElse(deserializationError(deserializationErrorMessage))
      case _ => deserializationError(deserializationErrorMessage)
    }

    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    private val deserializationErrorMessage =
      s"Expected date time in ISO offset date time format ex. ${LocalDateTime.now().format(formatter)}"
  }

  implicit object CoordinatesCharacterMapFormat extends RootJsonFormat[Map[HexCoordinates, CharacterId]] {
    override def write(obj: Map[HexCoordinates, CharacterId]) = {
      val m: List[JsField] = obj.map{case (coordinates, characterId) => (coordinates.toJson.toString(), JsString(characterId))}.toList
      JsObject(m:_*)
    }

    override def read(json: JsValue) = json match {
      case JsObject(fields) => fields.map{case (coordinates, characterId) => (coordinates.parseJson.convertTo[HexCoordinates], characterId.convertTo[String])}
      case x => deserializationError(s"Expected object, but got $x.")
    }
  }
}
