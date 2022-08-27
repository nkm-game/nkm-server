package unit.abilities.roronoa_zoro

import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.abilities.roronoa_zoro.LackOfOrientation
import com.tosware.nkm.models.game.hex.HexCoordinates
import com.tosware.nkm.models.game.hex.HexUtils.CoordinateSeq
import com.tosware.nkm.providers.HexMapProvider.TestHexMapName
import helpers.TestUtils
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class LackOfOrientationSpec
  extends AnyWordSpecLike
    with Matchers
    with TestUtils
{
  // TODO: override seed
  //  val lostSeed = 1337
  //  override implicit val random = new Random(lostSeed)

  val metadata = CharacterMetadata.empty().copy(initialAbilitiesMetadataIds = Seq(LackOfOrientation.metadata.id))
  val gameState = getTestGameState(TestHexMapName.Simple2v2, Seq(
    Seq(metadata.copy(name = "Empty1"), metadata.copy(name = "Empty2")),
    Seq(metadata.copy(name = "Empty3"), metadata.copy(name = "Empty4")),
  ))

  val p0FirstCharacterId = characterIdOnPoint(HexCoordinates(0, 0))(gameState)
  val p0SecondCharacterId = characterIdOnPoint(HexCoordinates(-1, 0))(gameState)

  val p1FirstCharacterId = characterIdOnPoint(HexCoordinates(3, 0))(gameState)
  val p1SecondCharacterId = characterIdOnPoint(HexCoordinates(4, 0))(gameState)

  LackOfOrientation.metadata.name must {
    "make parent character get lost sometimes" in {
      def move() = gameState.basicMoveCharacter(p0FirstCharacterId, CoordinateSeq((0, 0), (1, 0), (2, 0), (2, 1), (1, 1)))
      def moveAndGetParentCoords() = {
        val moveGameState = move()
        moveGameState.characterById(p0FirstCharacterId).get.parentCell(moveGameState).get.coordinates
      }
      val results = (0 to 50).map(_ => moveAndGetParentCoords()).map(_.toTuple)
      results.toSet.size should be > 2
    }
  }
}