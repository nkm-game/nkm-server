package unit

import com.tosware.nkm._
import com.tosware.nkm.models.game.event.GameEvent.GameEvent
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import scala.reflect.runtime.universe._
import scala.io.Source
import scala.util.Using

class ImplementationCheckerSpec
  extends AnyWordSpecLike
    with Matchers
    with Logging
{

  "Scala files in project" must {
    "implement all events in NkmJsonProtocol" in {
      // Get the names of all classes that derive from GameEvent
      val traitType = typeOf[GameEvent]
      val subTypes = traitType.typeSymbol.asClass.knownDirectSubclasses
      val subTypeNames = subTypes.map(_.name.toString)

      val nkmJsonProtocolFilePath = """src\main\scala\com\tosware\nkm\serializers\NkmJsonProtocol.scala"""
      val nkmJsonProtocolFileContents =  Using(Source.fromFile(nkmJsonProtocolFilePath))(_.mkString).get

      val writeClassNames = """case e: (.*) => GameEventSerialized\(e\.getClass\.getSimpleName""".r
        .findAllMatchIn(nkmJsonProtocolFileContents).map(_.group(1)).toSet
      val readClassNames = """case "(.*)" => ges.eventJson""".r
        .findAllMatchIn(nkmJsonProtocolFileContents).map(_.group(1)).toSet

      subTypeNames.diff(writeClassNames) shouldBe empty
      subTypeNames.diff(readClassNames) shouldBe empty
    }
  }
}
