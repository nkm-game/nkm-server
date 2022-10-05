package unit.abilities.blank

import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.abilities.blank.Check
import helpers.{TestUtils, scenarios}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class CheckSpec
  extends AnyWordSpecLike
    with Matchers
    with TestUtils
{
  private val abilityMetadata = Check.metadata
  private val characterMetadata = CharacterMetadata.empty().copy(initialAbilitiesMetadataIds = Seq(abilityMetadata.id))
  private val s = scenarios.Simple2v2TestScenario(characterMetadata)
  private implicit val gameState: GameState = s.gameState
  private val abilityId = s.characters.p0First.state.abilities.head.id

  abilityMetadata.name must {
    "be able to use" in {
      val r = GameStateValidator()
        .validateAbilityUseOnCharacter(s.characters.p0First.owner.id, abilityId, s.characters.p1First.id)
      assertCommandSuccess(r)
    }

    "force to take a turn" in {
      val abilityGameState: GameState = gameState
        .useAbilityOnCharacter(abilityId, s.characters.p1First.id)
        .endTurn()

      abilityGameState.characterTakingActionThisTurn should be (Some(s.characters.p1First.id))
    }

    "apply disarm" in {
      val abilityGameState: GameState = gameState
        .useAbilityOnCharacter(abilityId, s.characters.p1First.id)
        .endTurn()

      abilityGameState
        .characterById(s.characters.p1First.id).get
        .state.effects.exists(_.metadata.name == CharacterEffectName.Disarm)
    }

    "be unable to use on enemies that already took action" in {
      val passGameState: GameState = gameState
        .passTurn(s.characters.p0Second.id)
        .passTurn(s.characters.p1First.id)

      val r = GameStateValidator()(passGameState)
        .validateAbilityUseOnCharacter(s.characters.p0First.owner.id, abilityId, s.characters.p1First.id)
      assertCommandFailure(r)
    }
  }
}