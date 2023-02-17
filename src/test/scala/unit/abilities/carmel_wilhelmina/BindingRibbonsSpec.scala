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
  private val abilityId = s.characters.p0.state.abilities.head.id

  abilityMetadata.name must {
    "silence hit enemies" in {
      val abilityUsedGameState: GameState = s.gameState.useAbilityOnCoordinates(abilityId, s.spawnCoordinates.p0)
      abilityUsedGameState.characterById(s.characters.p1.head.id)
        .state.effects.ofType[effects.Silence] should not be empty
    }

    "silence and snare hit enemies when enough of them are hit" in {
      val twoHitGameState: GameState = s.gameState.useAbilityOnCoordinates(abilityId, HexCoordinates(-1, 0))
      twoHitGameState.characterById(s.characters.p1.head.id)
        .state.effects.ofType[effects.Snare] should be (empty)

      val threeHitGameState: GameState = s.gameState.useAbilityOnCoordinates(abilityId, s.spawnCoordinates.p0)
      val hitCharacterEffects = threeHitGameState.characterById(s.characters.p1.head.id).state.effects
      hitCharacterEffects.ofType[effects.Silence] should not be empty
      hitCharacterEffects.ofType[effects.Snare] should not be empty
    }
  }
}