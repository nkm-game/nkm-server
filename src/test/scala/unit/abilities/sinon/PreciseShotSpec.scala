package unit.abilities.sinon

import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.abilities.sinon.PreciseShot
import com.tosware.nkm.models.game.ability.UseData
import com.tosware.nkm.models.game.event.GameEvent
import com.tosware.nkm.models.game.hex.TestHexMapName
import helpers.{TestScenario, TestUtils}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class PreciseShotSpec
    extends AnyWordSpecLike
    with Matchers
    with TestUtils {
  private val abilityMetadata = PreciseShot.metadata
  private val s = TestScenario.generate(TestHexMapName.Simple2v2, abilityMetadata.id)
  private val aGs: GameState = s.ultGs.useAbility(s.defaultAbilityId, UseData(s.defaultEnemy.id))

  abilityMetadata.name must {
    "be able to use" in {
      assertCommandSuccess {
        GameStateValidator()(s.ultGs)
          .validateAbilityUse(s.owners(0), s.defaultAbilityId, UseData(s.defaultEnemy.id))
      }
    }

    "be able to deal damage" in {
      aGs
        .gameLog
        .events
        .ofType[GameEvent.CharacterDamaged]
        .map(_.causedById) should contain only s.defaultAbilityId
    }
  }
}
