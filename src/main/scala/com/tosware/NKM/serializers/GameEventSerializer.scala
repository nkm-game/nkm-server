// https://github.com/calvinlfer/Akka-persistence-example-with-custom-serializer

package com.tosware.NKM.serializers

import akka.serialization.Serializer
import com.tosware.NKM.actors.Game.Event
import spray.json._

class GameEventSerializer extends Serializer with NKMJsonProtocol {
  override def identifier: Int = 234

  override def includeManifest: Boolean = false

  override def fromBinary(bytes: Array[Byte], manifest: Option[Class[_]]): Event = {
    val jsonAst = new String(bytes).parseJson
    EventJsonFormat.read(jsonAst)
  }

  override def toBinary(o: AnyRef): Array[Byte] = o match {
    case e: Event => EventJsonFormat.write(e).compactPrint.getBytes
    case other => serializationError(s"Cannot serialize Basket Event: $other with $getClass")
  }
}
