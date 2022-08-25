package unit.abilities.roronoa_zoro

import com.tosware.NKM.models.game._
import com.tosware.NKM.models.game.abilities.roronoa_zoro.LackOfOrientation
import com.tosware.NKM.models.game.hex.HexCoordinates
import com.tosware.NKM.models.game.hex.HexUtils.CoordinateSeq
import com.tosware.NKM.providers.HexMapProvider.TestHexMapName
import helpers.TestUtils
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import scala.util.Random

class LackOfOrientationSpec
  extends AnyWordSpecLike
    with Matchers
    with TestUtils
{
  val lostSeed = 1337
  override implicit val random = new Random(lostSeed)

  val metadata = CharacterMetadata.empty().copy(initialAbilitiesMetadataIds = Seq(LackOfOrientation.metadata.id))
  implicit val gameState = getTestGameState(TestHexMapName.Simple2v2, Seq(
    Seq(metadata.copy(name = "Empty1"), metadata.copy(name = "Empty2")),
    Seq(metadata.copy(name = "Empty3"), metadata.copy(name = "Empty4")),
  ))

  val p0FirstCharacterId = characterIdOnPoint(HexCoordinates(0, 0))
  val p0SecondCharacterId = characterIdOnPoint(HexCoordinates(-1, 0))

  val p1FirstCharacterId = characterIdOnPoint(HexCoordinates(3, 0))
  val p1SecondCharacterId = characterIdOnPoint(HexCoordinates(4, 0))

  LackOfOrientation.metadata.name must {
    "make parent character get lost sometimes" in {
      def move() = gameState.basicMoveCharacter(p0FirstCharacterId, CoordinateSeq((0, 0), (1, 1)))
      def moveAndGetParentCoords() = move().characterById(p0FirstCharacterId).get.parentCell.get.coordinates
      moveAndGetParentCoords() shouldBe (1, 1)
      moveAndGetParentCoords() shouldBe (1, 1)
      moveAndGetParentCoords() shouldBe (1, 1)
      moveAndGetParentCoords() shouldBe (0, 1)
      moveAndGetParentCoords() shouldBe (1, 1)
      moveAndGetParentCoords() shouldBe (1, 1)
    }
  }
}