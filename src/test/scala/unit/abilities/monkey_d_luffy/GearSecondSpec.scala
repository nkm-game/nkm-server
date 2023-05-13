package unit.abilities.monkey_d_luffy

import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.abilities.monkey_d_luffy.GearSecond
import com.tosware.nkm.models.game.ability.AbilityType
import com.tosware.nkm.models.game.character.StatType
import com.tosware.nkm.models.game.effects.AbilityEnchant
import com.tosware.nkm.models.game.hex.TestHexMapName
import helpers.{TestScenario, TestUtils}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class GearSecondSpec
  extends AnyWordSpecLike
    with Matchers
    with TestUtils
{
  private val abilityMetadata = GearSecond.metadata
  private val s = TestScenario.generate(TestHexMapName.Simple1v1, abilityMetadata.id)
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

    "enchant normal ability" in {
      val enchantEffects =
        aGs
          .characterById(s.defaultCharacter.id)
          .state
          .effects
          .ofType[AbilityEnchant]

      enchantEffects should not be empty
      enchantEffects.head.abilityType should be (AbilityType.Normal)
    }

    "apply speed buff" in {
      assertBuffExists(StatType.Speed, s.defaultCharacter.id)(aGs)
    }
  }
}