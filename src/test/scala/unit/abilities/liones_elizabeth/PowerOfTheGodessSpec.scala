package unit.abilities.liones_elizabeth

import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.abilities.liones_elizabeth.PowerOfTheGoddess
import com.tosware.nkm.models.game.character.CharacterMetadata
import com.tosware.nkm.models.game.event.GameEvent
import helpers.{TestUtils, scenarios}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class PowerOfTheGodessSpec
  extends AnyWordSpecLike
    with Matchers
    with TestUtils
{
  private val abilityMetadata = PowerOfTheGoddess.metadata
  private val metadata = CharacterMetadata.empty().copy(initialAbilitiesMetadataIds = Seq(abilityMetadata.id))
  private val s = scenarios.Simple2v2TestScenario(metadata)
  private val gameState: GameState = s.gameState.incrementPhase(4)
  private val abilityId = s.characters.p0First.state.abilities.head.id

  abilityMetadata.name must {
    "be able to use" in {
      assertCommandSuccess {
        GameStateValidator()(gameState)
          .validateAbilityUse(s.characters.p0First.owner(gameState).id, abilityId)
      }
    }

    "be able to heal characters" in {
      val ags: GameState = gameState.useAbility(abilityId)

      ags.gameLog.events
        .ofType[GameEvent.CharacterHealed]
        .causedBy(abilityId)
        .size should be (2)
    }
  }
}