package com.tosware.NKM.serializers

import com.tosware.NKM.models._
import com.tosware.NKM.models.game.{HexCellEffect, HexCellType, Normal, SpawnPoint, Transparent, Wall}
import pl.iterators.kebs.json.KebsSpray
import spray.json._

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.util.Try

trait NKMJsonProtocol
  extends DefaultJsonProtocol
    with KebsSpray
    with KebsSpray.NoFlat // jwt serialize / deserialize does not work with flat serialization (idk why)
{

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

  // borrowed from https://github.com/theiterators/kebs/blob/master/examples/src/main/scala/pl/iterators/kebs_examples/SprayJsonWithAkkaHttpExample.scala
  implicit object LocalDateFormat extends RootJsonFormat[LocalDate] {
    override def write(obj: LocalDate) = JsString(formatter.format(obj))

    override def read(json: JsValue) = json match {
      case JsString(lDString) =>
        Try(LocalDate.parse(lDString, formatter)).getOrElse(deserializationError(deserializationErrorMessage))
      case _ => deserializationError(deserializationErrorMessage)
    }

    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val deserializationErrorMessage =
      s"Expected date time in ISO offset date time format ex. ${LocalDate.now().format(formatter)}"
  }
}
