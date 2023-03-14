package unit.abilities.kazuma

import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.abilities.satou_kazuma.Steal
import com.tosware.nkm.models.game.hex.TestHexMapName
import helpers.{TestScenario, TestUtils}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class StealSpec
  extends AnyWordSpecLike
    with Matchers
    with TestUtils
{
  private val abilityMetadata = Steal.metadata
  private val s = TestScenario.generate(TestHexMapName.Simple1v1, abilityMetadata.id)
  private val aGs: GameState =
    s.ultGs.useAbilityOnCharacter(s.defaultAbilityId, s.p(1)(0).character.id)

  abilityMetadata.name must {
    "be able to use" in {
      assertCommandSuccess {
        GameStateValidator()(s.ultGs)
          .validateAbilityUseOnCharacter(s.owners(0), s.defaultAbilityId, s.p(1)(0).character.id)
      }
    }

    "be able to steal armor" in {
      aGs.characterById(s.defaultCharacter.id).state.pureMagicalDefense should be (s.defaultCharacter.state.pureMagicalDefense * 2)
      aGs.characterById(s.defaultCharacter.id).state.purePhysicalDefense should be (s.defaultCharacter.state.purePhysicalDefense * 2)

      aGs.characterById(s.p(1)(0).character.id).state.pureMagicalDefense should be (0)
      aGs.characterById(s.p(1)(0).character.id).state.purePhysicalDefense should be (0)
    }
  }
}