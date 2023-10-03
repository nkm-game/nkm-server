package unit.abilities.blank

import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.abilities.blank.Check
import com.tosware.nkm.models.game.character.CharacterMetadata
import com.tosware.nkm.models.game.character_effect.CharacterEffectName
import helpers.{TestUtils, scenarios}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class CheckSpec
    extends AnyWordSpecLike
    with Matchers
    with TestUtils {
  private val abilityMetadata = Check.metadata
  private val characterMetadata = CharacterMetadata.empty().copy(initialAbilitiesMetadataIds = Seq(abilityMetadata.id))
  private val s = scenarios.Simple2v2TestScenario(characterMetadata)
  implicit private val gameState: GameState = s.gameState
  private val abilityId = s.p(0)(0).character.state.abilities.head.id

  abilityMetadata.name must {
    "be able to use" in {
      val r = GameStateValidator()
        .validateAbilityUseOnCharacter(s.p(0)(0).character.owner.id, abilityId, s.p(1)(0).character.id)
      assertCommandSuccess(r)
    }

    "force to take a turn" in {
      val abilityGameState: GameState = gameState
        .useAbilityOnCharacter(abilityId, s.p(1)(0).character.id)
        .endTurn()

      abilityGameState.characterTakingActionThisTurn should be(Some(s.p(1)(0).character.id))
    }

    "apply disarm" in {
      val abilityGameState: GameState = gameState
        .useAbilityOnCharacter(abilityId, s.p(1)(0).character.id)
        .endTurn()

      abilityGameState
        .characterById(s.p(1)(0).character.id)
        .state.effects.exists(_.metadata.name == CharacterEffectName.Disarm)
    }

    "be unable to use on enemies that already took action" in {
      val passGameState: GameState = gameState
        .passTurn(s.p(0)(1).character.id)
        .passTurn(s.p(1)(0).character.id)

      val r = GameStateValidator()(passGameState)
        .validateAbilityUseOnCharacter(s.p(0)(0).character.owner.id, abilityId, s.p(1)(0).character.id)
      assertCommandFailure(r)
    }
  }
}
