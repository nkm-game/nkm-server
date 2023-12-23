package unit.abilities.kazuma

import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.abilities.satou_kazuma.DrainTouch
import com.tosware.nkm.models.game.ability.UseData
import com.tosware.nkm.models.game.event.GameEvent
import com.tosware.nkm.models.game.hex.TestHexMapName
import helpers.{TestScenario, TestUtils}

class DrainTouchSpec extends TestUtils {
  private val abilityMetadata = DrainTouch.metadata
  private val s = TestScenario.generate(TestHexMapName.Simple1v1, abilityMetadata.id)
  private val aGs: GameState =
    s.gameState
      .damageCharacter(s.defaultCharacter.id, Damage(DamageType.True, 10))
      .useAbility(s.defaultAbilityId, UseData(s.defaultEnemy.id))

  abilityMetadata.name must {
    "be able to use" in {
      assertCommandSuccess {
        GameStateValidator()(s.gameState)
          .validateAbilityUse(s.owners(0), s.defaultAbilityId, UseData(s.defaultEnemy.id))
      }
    }

    "be able to deal damage" in {
      aGs
        .gameLog
        .events
        .ofType[GameEvent.CharacterDamaged]
        .map(_.causedById) should contain(s.defaultAbilityId)
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
