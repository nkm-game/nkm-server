package unit.abilities.sinon

import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.abilities.sinon.TacticalEscape
import com.tosware.nkm.models.game.character.CharacterMetadata
import helpers.{TestUtils, scenarios}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class TacticalEscapeSpec
  extends AnyWordSpecLike
    with Matchers
    with TestUtils
{
  private val abilityMetadata = TacticalEscape.metadata
  private val metadata = CharacterMetadata.empty()
    .copy(initialAbilitiesMetadataIds = Seq(abilityMetadata.id))
  private val s = scenarios.Simple2v2TestScenario(metadata)
  private implicit val gameState: GameState = s.gameState
  private val abilityId = s.characters.p0First.state.abilities.head.id

  abilityMetadata.name must {
    "be able to use" in {
      assertCommandSuccess {
        GameStateValidator()
          .validateAbilityUse(
            s.characters.p0First.owner.id,
            abilityId,
          )
      }
    }

    "be able to modify character speed" in {
      val abilityUsedGameState: GameState = gameState.useAbility(abilityId)
      val oldSpeed = s.characters.p0First.state.speed
      val newSpeed = abilityUsedGameState.characterById(s.characters.p0First.id).state.speed
      oldSpeed should be < newSpeed
    }
  }
}