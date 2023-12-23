package unit.abilities.ebisuzawa_kurumi

import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.abilities.ebisuzawa_kurumi.Survivor
import com.tosware.nkm.models.game.hex.TestHexMapName
import helpers.{TestScenario, TestUtils}

class SurvivorSpec extends TestUtils {
  private val abilityMetadata = Survivor.metadata
  private val s = TestScenario.generate(TestHexMapName.Simple2v2, abilityMetadata.id)
  private val aGs = s.gameState.useAbility(s.defaultAbilityId)
  private val attackGs = aGs.basicAttack(s.defaultCharacter.id, s.defaultEnemy.id)

  abilityMetadata.name must {
    "apply basic attack buffs" in {
      assertEffectExistsOfType[effects.NextBasicAttackBuff](s.defaultCharacter.id)(aGs)

      assertEffectExistsOfType[effects.Stun](s.defaultEnemy.id)(attackGs)
      attackGs.characterById(s.defaultEnemy.id).isDead shouldBe false
    }

    "become invisible" in {
      assertEffectExistsOfType[effects.Invisibility](s.defaultCharacter.id)(aGs)
    }
  }
}
