package unit.abilities.ochaco_uraraka

import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game.abilities.ochaco_uraraka.ReducedWeight
import com.tosware.nkm.models.game.ability.UseData
import com.tosware.nkm.models.game.character.StatType
import com.tosware.nkm.models.game.character_effect.CharacterEffectName
import com.tosware.nkm.models.game.effects.Fly
import com.tosware.nkm.models.game.game_state.GameState
import com.tosware.nkm.models.game.hex.TestHexMapName
import helpers.{TestScenario, TestUtils}

class ReducedWeightSpec extends TestUtils {
  private val abilityMetadata = ReducedWeight.metadata
  private val s = TestScenario.generate(TestHexMapName.Simple2v2, abilityMetadata.id)
  private val aGs: GameState = s.gameState.useAbility(s.defaultAbilityId, UseData(s.defaultCharacter.id))

  abilityMetadata.name must {
    "be able to use" in {
      assertCommandSuccess {
        GameStateValidator()(s.gameState)
          .validateAbilityUse(s.owners(0), s.defaultAbilityId, UseData(s.defaultCharacter.id))

        GameStateValidator()(s.gameState)
          .validateAbilityUse(s.owners(0), s.defaultAbilityId, UseData(s.p(0)(1).character.id))
      }
    }

    "apply Zero Gravity" in {
      assertEffectExistsOfType[Fly](s.defaultCharacter.id)(aGs)
      assertEffectsExist(Seq(CharacterEffectName.ZeroGravity), s.defaultCharacter.id)(aGs)
    }

    "buff speed" in {
      assertBuffExists(StatType.Speed, s.defaultCharacter.id)(aGs)
    }
  }
}
