package com.tosware.NKM.serializers

import pl.iterators.kebs.json.{KebsEnumFormats, KebsSpray}
import spray.json._

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.util.Try

trait NKMJsonProtocol
  extends DefaultJsonProtocol
    with KebsSpray
    with KebsEnumFormats // enumeratum support
    with KebsSpray.NoFlat // jwt serialize / deserialize does not work with flat serialization (idk why)
{

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
