package unit.abilities.roronoa_zoro

import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.abilities.roronoa_zoro.OneHundredEightPoundPhoenix
import helpers.{Simple2v2TestScenario, TestUtils}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class OneHundredEightPoundPhoenixSpec
  extends AnyWordSpecLike
    with Matchers
    with TestUtils
{
  private val metadata = CharacterMetadata.empty().copy(initialAbilitiesMetadataIds = Seq(OneHundredEightPoundPhoenix.metadata.id))
  private val s = Simple2v2TestScenario(metadata)
  private implicit val gameState: GameState = s.gameState
  private val abilityId = s.characters.p0First.state.abilities.head.id

  OneHundredEightPoundPhoenix.metadata.name must {
    "be able to damage single character" in {
      fail()
    }
    "be able to damage several characters" in {
      fail()
    }
    "send shockwaves over friends" in {
      fail()
    }
  }
}