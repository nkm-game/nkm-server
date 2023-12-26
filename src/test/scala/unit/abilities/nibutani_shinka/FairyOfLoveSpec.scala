package unit.abilities.nibutani_shinka

import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game.abilities.nibutani_shinka.FairyOfLove
import com.tosware.nkm.models.game.ability.AbilityType
import com.tosware.nkm.models.game.effects.AbilityEnchant
import com.tosware.nkm.models.game.game_state.GameState
import com.tosware.nkm.models.game.hex.TestHexMapName
import helpers.{TestScenario, TestUtils}

class FairyOfLoveSpec extends TestUtils {
  private val abilityMetadata = FairyOfLove.metadata
  private val s = TestScenario.generate(TestHexMapName.Simple2v2, abilityMetadata.id)
  private val aGs: GameState = s.ultGs.useAbility(s.defaultAbilityId)

  abilityMetadata.name must {
    "be able to use" in {
      assertCommandSuccess {
        GameStateValidator()(s.ultGs)
          .validateAbilityUse(
            s.owners(0),
            s.defaultAbilityId,
          )
      }
    }

    "enchant passive ability" in {
      val enchantEffects =
        aGs
          .characterById(s.defaultCharacter.id)
          .state
          .effects
          .ofType[AbilityEnchant]

      enchantEffects should not be empty
      enchantEffects.head.abilityType should be(AbilityType.Passive)
    }
  }
}
