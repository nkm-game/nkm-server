package unit.abilities.ochaco_uraraka

import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.abilities.ochaco_uraraka.SkillRelease
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
  private val aGs: GameState = s.ultGs.useAbility(s.defaultAbilityId)

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
      fail()
    }

    "stun enemies that had Zero Gravity" in {
      fail()
    }
  }
}