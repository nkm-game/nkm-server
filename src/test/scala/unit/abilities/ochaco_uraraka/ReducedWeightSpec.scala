package unit.abilities.ochaco_uraraka

import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.abilities.ochaco_uraraka.ReducedWeight
import com.tosware.nkm.models.game.character.StatType
import com.tosware.nkm.models.game.character_effect.CharacterEffectName
import com.tosware.nkm.models.game.effects.Fly
import com.tosware.nkm.models.game.hex.TestHexMapName
import helpers.{TestScenario, TestUtils}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class ReducedWeightSpec
  extends AnyWordSpecLike
    with Matchers
    with TestUtils
{
  private val abilityMetadata = ReducedWeight.metadata
  private val s = TestScenario.generate(TestHexMapName.Simple2v2, abilityMetadata.id)
  private val aGs: GameState = s.gameState.useAbilityOnCharacter(s.defaultAbilityId, s.defaultCharacter.id)

  abilityMetadata.name must {
    "be able to use" in {
      assertCommandSuccess {
        GameStateValidator()(s.gameState)
          .validateAbilityUseOnCharacter(s.owners(0), s.defaultAbilityId, s.defaultCharacter.id)

        GameStateValidator()(s.gameState)
          .validateAbilityUseOnCharacter(s.owners(0), s.defaultAbilityId, s.p(0)(1).character.id)
      }
    }

    "apply Zero Gravity" in {
      assertEffectExistsOfType[Fly](s.defaultCharacter.id)(aGs)
      assertEffectsExist(Seq(CharacterEffectName.ZeroGravity), s.defaultCharacter.id)(aGs)
    }

    "buff speed" in {
      assertBuffExists(StatType.Speed,  s.defaultCharacter.id)(aGs)
    }
  }
}