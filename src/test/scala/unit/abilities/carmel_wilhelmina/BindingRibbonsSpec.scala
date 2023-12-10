package unit.abilities.carmel_wilhelmina

import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.abilities.carmel_wilhelmina.BindingRibbons
import com.tosware.nkm.models.game.ability.UseData
import com.tosware.nkm.models.game.hex.{HexCoordinates, TestHexMapName}
import helpers.{TestScenario, TestUtils}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class BindingRibbonsSpec
    extends AnyWordSpecLike
    with Matchers
    with TestUtils {
  private val abilityMetadata = BindingRibbons.metadata
  private val s = TestScenario.generate(TestHexMapName.Simple1v9Line, abilityMetadata.id)
  private val abilityId = s.defaultAbilityId

  private val twoHitGameState: GameState = s.gameState.useAbility(abilityId, UseData(HexCoordinates(-1, 0)))
  private val threeHitGameState: GameState = s.gameState.useAbility(abilityId, UseData(s.p(0)(0).spawnCoordinates))

  abilityMetadata.name must {
    "only silence hit enemies" in {
      assertEffectExistsOfType[effects.Silence](s.defaultEnemy.id)(twoHitGameState)
      assertEffectDoesNotExistOfType[effects.Snare](s.defaultEnemy.id)(twoHitGameState)
    }

    "silence and snare hit enemies when enough of them are hit" in {
      assertEffectExistsOfType[effects.Silence](s.defaultEnemy.id)(threeHitGameState)
      assertEffectExistsOfType[effects.Snare](s.defaultEnemy.id)(threeHitGameState)
    }
  }
}
