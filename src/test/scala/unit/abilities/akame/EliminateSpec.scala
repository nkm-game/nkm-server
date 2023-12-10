package unit.abilities.akame

import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.abilities.akame.Eliminate
import com.tosware.nkm.models.game.ability.UseData
import com.tosware.nkm.models.game.event.GameEvent.CharacterDamaged
import com.tosware.nkm.models.game.hex.TestHexMapName
import helpers.{TestScenario, TestUtils}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class EliminateSpec
    extends AnyWordSpecLike
    with Matchers
    with TestUtils {
  private val abilityMetadata = Eliminate.metadata
  private val s = TestScenario.generate(TestHexMapName.Simple1v1, abilityMetadata.id)
  private val abilityId = s.defaultAbilityId

  abilityMetadata.name must {
    "be able to use" in {
      assertCommandSuccess {
        GameStateValidator()(s.gameState)
          .validateAbilityUse(s.owners(0), abilityId, UseData(s.defaultEnemy.id))
      }
    }
    "deal damage" in {
      val newGameState: GameState = s.gameState.useAbility(abilityId, UseData(s.defaultEnemy.id))

      newGameState
        .gameLog
        .events
        .causedBy(abilityId)
        .ofType[CharacterDamaged] should not be empty
    }
  }
}
