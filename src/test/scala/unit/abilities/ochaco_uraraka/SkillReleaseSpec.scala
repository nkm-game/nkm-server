package unit.abilities.ochaco_uraraka

import com.tosware.nkm._
import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.abilities.ochaco_uraraka.SkillRelease
import com.tosware.nkm.models.game.effects.ZeroGravity
import com.tosware.nkm.models.game.hex.TestHexMapName
import helpers.{TestScenario, TestUtils}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class SkillReleaseSpec
  extends AnyWordSpecLike
    with Matchers
    with TestUtils
{
  private val abilityMetadata = SkillRelease.metadata
  private val s = TestScenario.generate(TestHexMapName.Simple2v2, abilityMetadata.id)

  private val flyGs: GameState =
    s.ultGs
      .addEffect(s.defaultCharacter.id, effects.Fly(randomUUID(), 10))
      .addEffect(s.p(1)(0).character.id, effects.Fly(randomUUID(), 10))
      .useAbility(s.defaultAbilityId)
  private val zeroGravityGs: GameState =
    s.ultGs
      .addEffect(s.defaultCharacter.id, effects.Fly(randomUUID(), 10, ZeroGravity.metadata))
      .addEffect(s.p(1)(0).character.id, effects.Fly(randomUUID(), 10, ZeroGravity.metadata))
      .useAbility(s.defaultAbilityId)

  abilityMetadata.name must {
    "be able to use" in {
      assertCommandSuccess {
        GameStateValidator()(s.ultGs)
          .validateAbilityUse(
            s.owners(0),
            s.defaultAbilityId,
          )
      }
    }

    "remove Zero Gravity effects" in {
      assertEffectDoesNotExistsOfType[effects.Fly](s.defaultCharacter.id)(zeroGravityGs)
      assertEffectDoesNotExistsOfType[effects.Fly](s.p(1)(0).character.id)(zeroGravityGs)
    }

    "not work unrelated Fly effects" in {
      assertEffectExistsOfType[effects.Fly](s.defaultCharacter.id)(flyGs)
      assertEffectExistsOfType[effects.Fly](s.p(1)(0).character.id)(flyGs)

      assertEffectDoesNotExistsOfType[effects.Stun](s.defaultCharacter.id)(flyGs)
      assertEffectDoesNotExistsOfType[effects.Stun](s.p(1)(0).character.id)(flyGs)
    }

    "stun enemies that had Zero Gravity" in {
      assertEffectDoesNotExistsOfType[effects.Stun](s.defaultCharacter.id)(zeroGravityGs)
      assertEffectExistsOfType[effects.Stun](s.p(1)(0).character.id)(zeroGravityGs)
    }
  }
}