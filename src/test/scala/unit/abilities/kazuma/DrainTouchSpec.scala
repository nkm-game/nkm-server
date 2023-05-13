package unit.abilities.kazuma

import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.abilities.satou_kazuma.DrainTouch
import com.tosware.nkm.models.game.event.GameEvent
import com.tosware.nkm.models.game.hex.TestHexMapName
import helpers.{TestScenario, TestUtils}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class DrainTouchSpec
  extends AnyWordSpecLike
    with Matchers
    with TestUtils
{
  private val abilityMetadata = DrainTouch.metadata
  private val s = TestScenario.generate(TestHexMapName.Simple1v1, abilityMetadata.id)
  private val aGs: GameState =
    s.gameState
      .damageCharacter(s.defaultCharacter.id, Damage(DamageType.True, 10))
      .useAbilityOnCharacter(s.defaultAbilityId, s.p(1)(0).character.id)

  abilityMetadata.name must {
    "be able to use" in {
      assertCommandSuccess {
        GameStateValidator()(s.gameState)
          .validateAbilityUseOnCharacter(s.owners(0), s.defaultAbilityId, s.p(1)(0).character.id)
      }
    }

    "be able to deal damage" in {
      aGs
        .gameLog
        .events
        .ofType[GameEvent.CharacterDamaged]
        .map(_.causedById) should contain (s.defaultAbilityId)
    }

    "be able to heal" in {
      aGs
        .gameLog
        .events
        .ofType[GameEvent.CharacterHealed]
        .map(_.causedById) should contain only s.defaultAbilityId
    }
  }
}