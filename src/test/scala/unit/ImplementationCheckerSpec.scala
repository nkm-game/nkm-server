package unit

import com.tosware.nkm.*
import com.tosware.nkm.models.game.event.GameEvent.GameEvent
import helpers.{NotWorkingOnCI, TestUtils}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import java.io.{File, FilenameFilter}
import scala.reflect.runtime.currentMirror as cm
import scala.reflect.runtime.universe.*

class ImplementationCheckerSpec
    extends AnyWordSpecLike
    with Matchers
    with Logging
    with TestUtils {
  "Scala files in project" must {
    "implement all events in NkmJsonProtocol" taggedAs NotWorkingOnCI in {
      // Get the names of all classes that derive from GameEvent
      val traitType = typeOf[GameEvent]
      val subTypes = traitType.typeSymbol.asClass.knownDirectSubclasses
      val subTypeNames = subTypes.map(_.name.toString)

      val nkmJsonProtocolFileContents =
        getFileContents("""src\main\scala\com\tosware\nkm\serializers\NkmJsonProtocol.scala""")

      val writeClassNames =
        findMatchingStrings(
          """case e: (\w+).* => GameEventSerialized\(e\.getClass\.getSimpleName""".r,
          nkmJsonProtocolFileContents,
        )

      val readClassNames =
        findMatchingStrings("""case "(\w+)".* => ges.eventJson""".r, nkmJsonProtocolFileContents)

      subTypeNames.diff(writeClassNames) shouldBe empty
      subTypeNames.diff(readClassNames) shouldBe empty
    }

    def testEventRecoveryCompleteness(eventTypeClass: Class[_], fileName: String): Unit = {
      val basePath = "src/main/scala/com/tosware/nkm/actors/"
      val fullPath = basePath + fileName

      val mirror = cm
      val symbol = mirror.classSymbol(eventTypeClass)
      val traitType = symbol.toType

      val subTypes = traitType.typeSymbol.asClass.knownDirectSubclasses
      val subTypeNames = subTypes.map(_.name.toString)

      val fileContents = getFileContents(fullPath)
      val usedInRecoveryMethod = findMatchingStrings("""case (\w+).* =>""".r, fileContents)

      subTypeNames.diff(usedInRecoveryMethod) shouldBe empty
    }

    val actorsDirectory = new File("src/main/scala/com/tosware/nkm/actors")
    val scalaFiles = actorsDirectory.listFiles(new FilenameFilter {
      override def accept(dir: File, name: String): Boolean = name.endsWith(".scala")
    }).map(_.getName)

    scalaFiles.foreach { fileName =>
      val className = fileName.replace(".scala", "")
      val testName = s"implement all events in the $className actor recovery"
      val eventType = Class.forName(s"com.tosware.nkm.actors.$className$$Event")

      testName taggedAs NotWorkingOnCI in {
        testEventRecoveryCompleteness(eventType, fileName)
      }
    }

    def testMetadataProvider(modelPath: String, providerName: String) = {
      val fileNames = readFileNames(s"src/main/scala/com/tosware/nkm/models/$modelPath").toSet

      val providerFileContents =
        getFileContents(s"""src/main/scala/com/tosware/nkm/providers/$providerName.scala""")

      val metadatasDefinedInProvider = findMatchingStrings("""(\w+).metadata""".r, providerFileContents)

      println(fileNames)
      println(metadatasDefinedInProvider)

      fileNames.diff(metadatasDefinedInProvider) shouldBe empty
    }

    "provide all ability metadatas in API" taggedAs NotWorkingOnCI in {
      testMetadataProvider("game/abilities", "AbilityMetadataProvider")
    }

    "provide all effect metadatas in API" taggedAs NotWorkingOnCI in {
      testMetadataProvider("game/effects", "CharacterEffectMetadataProvider")
    }

    "provide all hex effect metadatas in API" taggedAs NotWorkingOnCI in {
      testMetadataProvider("game/hex_effects", "HexCellEffectMetadataProvider")
    }
  }
}
