package unit.abilities.ebisuzawa_kurumi

import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.abilities.ebisuzawa_kurumi.FinalSolution
import com.tosware.nkm.models.game.character.CharacterMetadata
import helpers.{TestUtils, scenarios}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class FinalSolutionSpec
  extends AnyWordSpecLike
    with Matchers
    with TestUtils
{
  private val abilityMetadata = FinalSolution.metadata
  private val metadata = CharacterMetadata.empty().copy(initialAbilitiesMetadataIds = Seq(abilityMetadata.id))
  private val s = scenarios.Simple2v2TestScenario(metadata)
  private implicit val gameState: GameState = s.gameState.incrementPhase(4)
  private val abilityId = s.characters.p0First.state.abilities.head.id

  abilityMetadata.name must {
    "be able to use" in {
      assertCommandSuccess {
        GameStateValidator()
          .validateAbilityUseOnCharacter(s.characters.p0First.owner.id, abilityId, s.characters.p1First.id)
      }
    }

    "be able to deal damage" in {
      val ngs: GameState = gameState.useAbilityOnCharacter(abilityId, s.characters.p1First.id)
      ngs.gameLog.events.ofType[GameEvent.CharacterDamaged].exists(_.causedById == abilityId) should be (true)
    }

    "apply bleeding effect" in {
      val ngs: GameState = gameState.useAbilityOnCharacter(abilityId, s.characters.p1First.id)
      ngs.characterById(s.characters.p1First.id).state.effects.ofType[effects.Poison] should not be empty
    }
  }
}