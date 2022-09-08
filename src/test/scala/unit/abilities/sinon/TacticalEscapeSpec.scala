package unit.abilities.sinon

import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.abilities.sinon.TacticalEscape
import helpers.{TestUtils, scenarios}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class TacticalEscapeSpec
  extends AnyWordSpecLike
    with Matchers
    with TestUtils
{
  private val metadata = CharacterMetadata.empty().copy(initialAbilitiesMetadataIds = Seq(TacticalEscape.metadata.id))
  private val s = scenarios.Simple2v2TestScenario(metadata)
  private implicit val gameState: GameState = s.gameState
  private val abilityId = s.characters.p0First.state.abilities.head.id

  TacticalEscape.metadata.name must {
    "be able to modify character speed" in {
      val validator = GameStateValidator()
      val r = validator.validateAbilityUseWithoutTarget(s.characters.p0First.owner.id, abilityId)
      assertCommandSuccess(r)

      val abilityUsedGameState: GameState = gameState.useAbilityWithoutTarget(abilityId)
      s.characters.p0First.state.speed should be < abilityUsedGameState.characterById(s.characters.p0First.id).get.state.speed
    }
  }
}