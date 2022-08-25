package unit.abilities.roronoa_zoro

import com.tosware.NKM.models.game._
import com.tosware.NKM.models.game.abilities.roronoa_zoro.OgreCutter
import com.tosware.NKM.models.game.hex.HexCoordinates
import com.tosware.NKM.providers.HexMapProvider.TestHexMapName
import helpers.TestUtils
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class OgreCutterSpec
  extends AnyWordSpecLike
    with Matchers
    with TestUtils
{
  val metadata = CharacterMetadata.empty().copy(initialAbilitiesMetadataIds = Seq(OgreCutter.metadata.id))
  implicit val gameState = getTestGameState(TestHexMapName.Simple2v2, Seq(
    Seq(metadata.copy(name = "Empty1"), metadata.copy(name = "Empty2")),
    Seq(metadata.copy(name = "Empty3"), metadata.copy(name = "Empty4")),
  ))

  val p0FirstCharacter = characterOnPoint(HexCoordinates(0, 0))
  val p0SecondCharacter = characterOnPoint(HexCoordinates(-1, 0))

  val p1FirstCharacter = characterOnPoint(HexCoordinates(3, 0))
  val p1SecondCharacter = characterOnPoint(HexCoordinates(4, 0))

  OgreCutter.metadata.name must {
    "be able to damage and teleport" in {
      fail()
    }
    "not be able to use if teleport cell is not free to stand" in {
      fail()
    }
  }
}