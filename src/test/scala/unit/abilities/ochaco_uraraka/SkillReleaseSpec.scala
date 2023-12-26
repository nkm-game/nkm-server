package unit.abilities.ochaco_uraraka

import com.tosware.nkm.*
import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.abilities.ochaco_uraraka.SkillRelease
import com.tosware.nkm.models.game.effects.ZeroGravity
import com.tosware.nkm.models.game.game_state.GameState
import com.tosware.nkm.models.game.hex.TestHexMapName
import helpers.{TestScenario, TestUtils}

class SkillReleaseSpec extends TestUtils {
  private val abilityMetadata = SkillRelease.metadata
  private val s = TestScenario.generate(TestHexMapName.Simple2v2, abilityMetadata.id)

  private val flyGs: GameState =
    s.ultGs
      .addEffect(s.defaultCharacter.id, effects.Fly(randomUUID(), 10))
      .addEffect(s.defaultEnemy.id, effects.Fly(randomUUID(), 10))
      .useAbility(s.defaultAbilityId)
  private val zeroGravityGs: GameState =
    s.ultGs
      .addEffect(s.defaultCharacter.id, effects.Fly(randomUUID(), 10, ZeroGravity.metadata))
      .addEffect(s.defaultEnemy.id, effects.Fly(randomUUID(), 10, ZeroGravity.metadata))
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
      assertEffectDoesNotExistOfType[effects.Fly](s.defaultCharacter.id)(zeroGravityGs)
      assertEffectDoesNotExistOfType[effects.Fly](s.defaultEnemy.id)(zeroGravityGs)
    }

    "not work unrelated Fly effects" in {
      assertEffectExistsOfType[effects.Fly](s.defaultCharacter.id)(flyGs)
      assertEffectExistsOfType[effects.Fly](s.defaultEnemy.id)(flyGs)

      assertEffectDoesNotExistOfType[effects.Stun](s.defaultCharacter.id)(flyGs)
      assertEffectDoesNotExistOfType[effects.Stun](s.defaultEnemy.id)(flyGs)
    }

    "stun enemies that had Zero Gravity" in {
      assertEffectDoesNotExistOfType[effects.Stun](s.defaultCharacter.id)(zeroGravityGs)
      assertEffectExistsOfType[effects.Stun](s.defaultEnemy.id)(zeroGravityGs)
    }
  }
}
