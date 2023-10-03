package unit.abilities.roronoa_zoro

import com.tosware.nkm.*
import com.tosware.nkm.models.game.abilities.roronoa_zoro.LackOfOrientation
import com.tosware.nkm.models.game.character.CharacterMetadata
import com.tosware.nkm.models.game.event.GameEvent
import helpers.{TestUtils, scenarios}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class LackOfOrientationSpec
    extends AnyWordSpecLike
    with Matchers
    with TestUtils {
  // TODO: override seed
  //  val lostSeed = 1337
  //  override implicit val random = new Random(lostSeed)

  private val abilityMetadata = LackOfOrientation.metadata
  private val characterMetadata = CharacterMetadata.empty().copy(initialAbilitiesMetadataIds = Seq(abilityMetadata.id))
  private val s = scenarios.Simple2v2TestScenario(characterMetadata)

  abilityMetadata.name must {
    "make parent character get lost sometimes" in {
      def move() =
        s.gameState.basicMoveCharacter(s.p(0)(0).character.id, CoordinateSeq((0, 0), (1, 0), (2, 0), (2, 1), (1, 1)))
      def moveAndGetParentCoords() = {
        val moveGameState = move()
        val targetCoords = moveGameState.characterById(s.p(0)(0).character.id).parentCell(moveGameState).get.coordinates

        // verify if CharacterBasicMoved event was changed
        if (targetCoords.toTuple != (1, 1)) {
          moveGameState
            .gameLog.events
            .ofType[GameEvent.CharacterBasicMoved].last
            .path.last
            .toTuple should not be (1, 1)
        }

        targetCoords
      }
      val results = (0 to 50).map(_ => moveAndGetParentCoords()).map(_.toTuple)
      results.toSet.size should be > 2
    }
  }
}
