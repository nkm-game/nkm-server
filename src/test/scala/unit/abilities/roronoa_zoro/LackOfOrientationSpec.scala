package unit.abilities.roronoa_zoro

import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.abilities.roronoa_zoro.LackOfOrientation
import com.tosware.nkm.models.game.hex.HexUtils.CoordinateSeq
import helpers.{Simple2v2TestScenario, TestUtils}
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

  private val metadata = CharacterMetadata.empty().copy(initialAbilitiesMetadataIds = Seq(LackOfOrientation.metadata.id))
  private val s = Simple2v2TestScenario(metadata)

  LackOfOrientation.metadata.name must {
    "make parent character get lost sometimes" in {
      def move() = s.gameState.basicMoveCharacter(s.characters.p0First.id, CoordinateSeq((0, 0), (1, 0), (2, 0), (2, 1), (1, 1)))
      def moveAndGetParentCoords() = {
        val moveGameState = move()
        moveGameState.characterById(s.characters.p0First.id).get.parentCell(moveGameState).get.coordinates
      }
      val results = (0 to 50).map(_ => moveAndGetParentCoords()).map(_.toTuple)
      results.toSet.size should be > 2
    }
  }
}