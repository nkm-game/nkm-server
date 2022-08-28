package unit.abilities.roronoa_zoro

import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.abilities.roronoa_zoro.OgreCutter
import helpers.{Simple2v2TestScenario, TestUtils}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class OgreCutterSpec
  extends AnyWordSpecLike
    with Matchers
    with TestUtils
{
  private val metadata = CharacterMetadata.empty().copy(initialAbilitiesMetadataIds = Seq(OgreCutter.metadata.id))
  private val s = Simple2v2TestScenario(metadata)
  private implicit val gameState: GameState = s.gameState
  private val abilityId = s.characters.p0First.state.abilities.head.id

  OgreCutter.metadata.name must {
    "be able to damage and teleport" in {
      fail()
    }
    "not be able to use if teleport cell is not free to stand" in {
      fail()
    }
  }
}