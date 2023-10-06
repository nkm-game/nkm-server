package unit.abilities.ochaco_uraraka

import com.tosware.nkm.models.game.abilities.ochaco_uraraka.ZeroGravity
import com.tosware.nkm.models.game.character_effect.CharacterEffectName
import com.tosware.nkm.models.game.effects.Fly
import com.tosware.nkm.models.game.hex.TestHexMapName
import helpers.{TestScenario, TestUtils}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class ZeroGravitySpec
    extends AnyWordSpecLike
    with Matchers
    with TestUtils {
  private val abilityMetadata = ZeroGravity.metadata
  private val s = TestScenario.generate(TestHexMapName.Simple2v2, abilityMetadata.id)

  private val friendAttackedGs = s.gameState.basicAttack(s.p(0)(0).character.id, s.p(0)(1).character.id)
  private val enemyAttackedGs = s.gameState.basicAttack(s.p(0)(0).character.id, s.p(1)(0).character.id)

  abilityMetadata.name must {
    "be able to apply Zero Gravity on friends on basic attacks" in {
      friendAttackedGs
        .characterById(s.p(0)(0).character.id)
        .isFriendForC(s.p(0)(1).character.id)(friendAttackedGs) shouldBe true

      assertEffectExistsOfType[Fly](s.p(0)(1).character.id)(friendAttackedGs)
      assertEffectsExist(Seq(CharacterEffectName.ZeroGravity), s.p(0)(1).character.id)(friendAttackedGs)
    }
    "be able to apply Zero Gravity on enemies on basic attacks" in {
      enemyAttackedGs
        .characterById(s.p(0)(0).character.id)
        .isFriendForC(s.p(1)(0).character.id)(enemyAttackedGs) shouldBe false

      assertEffectExistsOfType[Fly](s.p(1)(0).character.id)(enemyAttackedGs)
      assertEffectsExist(Seq(CharacterEffectName.ZeroGravity), s.p(1)(0).character.id)(enemyAttackedGs)
    }
  }
}
