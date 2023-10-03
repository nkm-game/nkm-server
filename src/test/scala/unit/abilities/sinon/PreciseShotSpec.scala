package unit.abilities.sinon

import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.abilities.sinon.PreciseShot
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
  private val aGs: GameState = s.ultGs.useAbilityOnCharacter(s.defaultAbilityId, s.p(1)(0).character.id)

  abilityMetadata.name must {
    "be able to use" in {
      assertCommandSuccess {
        GameStateValidator()(s.ultGs)
          .validateAbilityUseOnCharacter(s.owners(0), s.defaultAbilityId, s.p(1)(0).character.id)
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
