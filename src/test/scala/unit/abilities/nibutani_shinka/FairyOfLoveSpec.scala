package unit.abilities.nibutani_shinka

import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.abilities.nibutani_shinka.FairyOfLove
import com.tosware.nkm.models.game.ability.AbilityType
import com.tosware.nkm.models.game.character.CharacterMetadata
import com.tosware.nkm.models.game.effects.AbilityEnchant
import helpers.{TestUtils, scenarios}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class FairyOfLoveSpec
  extends AnyWordSpecLike
    with Matchers
    with TestUtils
{
  private val abilityMetadata = FairyOfLove.metadata
  private val metadata = CharacterMetadata.empty()
    .copy(initialAbilitiesMetadataIds = Seq(abilityMetadata.id))
  private val s = scenarios.Simple2v2TestScenario(metadata)
  private implicit val gameState: GameState = s.gameState.incrementPhase(4)
  private val abilityId = s.characters.p0First.state.abilities.head.id

  abilityMetadata.name must {
    "be able to use" in {
      assertCommandSuccess {
        GameStateValidator()
          .validateAbilityUse(
            s.characters.p0First.owner.id,
            abilityId,
          )
      }
    }

    "enchant passive ability" in {
      val abilityUsedGameState: GameState = gameState.useAbility(abilityId)
      val enchantEffects = abilityUsedGameState.characterById(s.characters.p0First.id).state.effects.ofType[AbilityEnchant]
      enchantEffects should not be empty
      enchantEffects.head.abilityType should be (AbilityType.Passive)
    }
  }
}