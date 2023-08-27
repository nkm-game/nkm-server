package unit.abilities.aqua

import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game.abilities.aqua.NaturesBeauty
import com.tosware.nkm.models.game.hex.TestHexMapName
import helpers.{TestScenario, TestUtils}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class NaturesBeautySpec
  extends AnyWordSpecLike
    with Matchers
    with TestUtils
{
  private val abilityMetadata = NaturesBeauty.metadata
  private val s = TestScenario.generate(TestHexMapName.Simple2v2, abilityMetadata.id)

  private val damagedGameState = s.gameState.setHp(s.p(0)(1).character.id, 30)(random, s.gameState.id)
  private val healedGameState = damagedGameState.basicAttack(s.defaultCharacter.id, s.p(0)(1).character.id)

  abilityMetadata.name must {
    "be able to target friends via basic attacks" in {
      assertCommandSuccess {
        GameStateValidator()(damagedGameState)
          .validateBasicAttackCharacter(s.owners(0), s.defaultCharacter.id, s.p(0)(1).character.id)
      }
    }
    "be able to heal friends via basic attacks" in {
      healedGameState.characterById(s.defaultCharacter.id)
        .isFriendForC(s.p(0)(1).character.id)(healedGameState) shouldBe true

      healedGameState.characterById(s.p(0)(1).character.id)
        .state.healthPoints should be(30 + healedGameState.characterById(s.p(0)(0).character.id).state.attackPoints)
    }
    "not show parent character as basic attack target" in {
      val parentCharacter = damagedGameState.characterById(s.defaultCharacter.id)
      parentCharacter.basicAttackTargets(damagedGameState) should not contain s.defaultCharacter.parentCell(damagedGameState).get.coordinates
    }
  }
}