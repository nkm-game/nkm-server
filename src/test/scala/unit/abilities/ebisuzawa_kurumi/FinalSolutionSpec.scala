package unit.abilities.ebisuzawa_kurumi

import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.abilities.ebisuzawa_kurumi.FinalSolution
import com.tosware.nkm.models.game.ability.UseData
import com.tosware.nkm.models.game.event.GameEvent
import com.tosware.nkm.models.game.hex.TestHexMapName
import helpers.{TestScenario, TestUtils}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class FinalSolutionSpec
    extends AnyWordSpecLike
    with Matchers
    with TestUtils {
  private val abilityMetadata = FinalSolution.metadata
  private val s = TestScenario.generate(TestHexMapName.Simple2v2, abilityMetadata.id)
  private val abilityId = s.p(0)(1).character.state.abilities.head.id

  private val damagedGs: GameState =
    s.ultGs
      .damageCharacter(s.defaultEnemy.id, Damage(DamageType.True, 20))

  private val aGs: GameState =
    damagedGs
      .useAbility(abilityId, UseData(s.defaultEnemy.id))

  abilityMetadata.name must {
    "be able to use" in {
      assertCommandSuccess {
        GameStateValidator()(damagedGs)
          .validateAbilityUse(s.owners(0), abilityId, UseData(s.defaultEnemy.id))
      }
    }

    "be able to deal damage" in {
      aGs
        .gameLog
        .events
        .ofType[GameEvent.CharacterDamaged]
        .map(_.causedById) should contain(abilityId)
    }

    "apply bleeding effect" in {
      assertEffectExistsOfType[effects.Poison](s.defaultEnemy.id)(aGs)
    }
  }
}
