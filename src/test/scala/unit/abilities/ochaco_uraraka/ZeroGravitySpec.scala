package unit.abilities.ochaco_uraraka

import com.tosware.nkm.models.game.abilities.ochaco_uraraka.ZeroGravity
import com.tosware.nkm.models.game.effects.Fly
import com.tosware.nkm.models.game.hex.TestHexMapName
import helpers.{TestScenario, TestUtils}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class ZeroGravitySpec
  extends AnyWordSpecLike
    with Matchers
    with TestUtils
{
  private val abilityMetadata = ZeroGravity.metadata
  private val s = TestScenario.generate(TestHexMapName.Simple2v2, abilityMetadata.id)

  private val attackedGs = s.gameState.basicAttack(s.p(0)(0).character.id, s.p(0)(1).character.id)

  abilityMetadata.name must {
    "be able to apply Zero Gravity on friends via basic attacks" in {
      attackedGs
        .characterById(s.p(0)(0).character.id)
        .isFriendForC(s.p(0)(1).character.id)(attackedGs) shouldBe true

      assertEffectExistsOfType[Fly](s.p(0)(1).character.id)(attackedGs)
    }
  }
}