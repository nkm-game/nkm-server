package unit.abilities.carmel_wilhelmina

import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.abilities.carmel_wilhelmina.BindingRibbons
import com.tosware.nkm.models.game.character.CharacterMetadata
import com.tosware.nkm.models.game.hex.HexCoordinates
import helpers.{TestUtils, scenarios}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class BindingRibbonsSpec
  extends AnyWordSpecLike
    with Matchers
    with TestUtils
{
  private val abilityMetadata = BindingRibbons.metadata
  private val characterMetadata = CharacterMetadata.empty().copy(initialAbilitiesMetadataIds = Seq(abilityMetadata.id))
  private val s = scenarios.Simple1v9LineTestScenario(characterMetadata)
  private implicit val gameState: GameState = s.gameState
  private val abilityId = s.p(0)(0).character.state.abilities.head.id

  private val twoHitGameState: GameState = s.gameState.useAbilityOnCoordinates(abilityId, HexCoordinates(-1, 0))
  private val threeHitGameState: GameState = s.gameState.useAbilityOnCoordinates(abilityId, s.p(0)(0).spawnCoordinates)

  abilityMetadata.name must {
    "only silence hit enemies" in {
      assertEffectExistsOfType[effects.Silence](s.p(1)(0).character.id)(twoHitGameState)
      assertEffectDoesNotExistsOfType[effects.Snare](s.p(1)(0).character.id)(twoHitGameState)
    }

    "silence and snare hit enemies when enough of them are hit" in {
      assertEffectExistsOfType[effects.Silence](s.p(1)(0).character.id)(threeHitGameState)
      assertEffectExistsOfType[effects.Snare](s.p(1)(0).character.id)(threeHitGameState)
    }
  }
}