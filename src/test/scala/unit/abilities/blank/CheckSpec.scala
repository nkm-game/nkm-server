package unit.abilities.blank

import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.abilities.blank.Check
import com.tosware.nkm.models.game.ability.UseData
import com.tosware.nkm.models.game.character.CharacterMetadata
import com.tosware.nkm.models.game.character_effect.CharacterEffectName
import com.tosware.nkm.models.game.hex.TestHexMapName
import helpers.{TestScenario, TestUtils}

class CheckSpec extends TestUtils {
  private val abilityMetadata = Check.metadata
  private val characterMetadata = CharacterMetadata.empty().copy(initialAbilitiesMetadataIds = Seq(abilityMetadata.id))
  private val s = TestScenario.generate(TestHexMapName.Simple2v2, characterMetadata)
  implicit private val gameState: GameState = s.gameState
  private val abilityId = s.defaultAbilityId

  abilityMetadata.name must {
    "be able to use" in {
      val r = GameStateValidator()
        .validateAbilityUse(s.owners(0), abilityId, UseData(s.defaultEnemy.id))
      assertCommandSuccess(r)
    }

    "force to take a turn" in {
      val abilityGameState: GameState = gameState
        .useAbility(abilityId, UseData(s.defaultEnemy.id))
        .endTurn()

      abilityGameState.characterTakingActionThisTurnOpt should be(Some(s.defaultEnemy.id))
    }

    "apply disarm" in {
      val abilityGameState: GameState = gameState
        .useAbility(abilityId, UseData(s.defaultEnemy.id))
        .endTurn()

      abilityGameState
        .characterById(s.defaultEnemy.id)
        .state.effects.exists(_.metadata.name == CharacterEffectName.Disarm)
    }

    "be unable to use on enemies that already took action" in {
      val passGameState: GameState = gameState
        .passTurn(s.p(0)(1).character.id)
        .passTurn(s.defaultEnemy.id)

      val r = GameStateValidator()(passGameState)
        .validateAbilityUse(s.owners(0), abilityId, UseData(s.defaultEnemy.id))
      assertCommandFailure(r)
    }
  }
}
