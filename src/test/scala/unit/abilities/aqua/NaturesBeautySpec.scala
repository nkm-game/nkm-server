package unit.abilities.aqua

import com.tosware.nkm.models.game.abilities.aqua.NaturesBeauty
import com.tosware.nkm.models.game.character.CharacterMetadata
import helpers.{TestUtils, scenarios}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class NaturesBeautySpec
  extends AnyWordSpecLike
    with Matchers
    with TestUtils
{
  private val abilityMetadata = NaturesBeauty.metadata
  private val metadata = CharacterMetadata.empty().copy(initialAbilitiesMetadataIds = Seq(abilityMetadata.id))
  private val s = scenarios.Simple2v2TestScenario(metadata)

  abilityMetadata.name must {
    "be able to heal friends via basic attacks" in {
      val damagedGameState = s.gameState.setHp(s.p(0)(1).character.id, 30)(random, s.gameState.id)
      val healedGameState = damagedGameState.basicAttack(s.p(0)(0).character.id, s.p(0)(1).character.id)

      healedGameState.characterById(s.p(0)(0).character.id)
        .isFriendForC(s.p(0)(1).character.id)(healedGameState) shouldBe true

      healedGameState.characterById(s.p(0)(1).character.id)
        .state.healthPoints should be(30 + healedGameState.characterById(s.p(0)(0).character.id).state.attackPoints)
    }
  }
}