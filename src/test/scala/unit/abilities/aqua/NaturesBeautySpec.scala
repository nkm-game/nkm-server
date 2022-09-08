package unit.abilities.aqua

import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.abilities.aqua.NaturesBeauty
import helpers.{TestUtils, scenarios}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class NaturesBeautySpec
  extends AnyWordSpecLike
    with Matchers
    with TestUtils
{
  private val metadata = CharacterMetadata.empty().copy(initialAbilitiesMetadataIds = Seq(NaturesBeauty.metadata.id))
  private val s = scenarios.Simple2v2TestScenario(metadata)

  NaturesBeauty.metadata.name must {
    "be able to heal friends via basic attacks" in {
      val damagedGameState = s.gameState.setHp(s.characters.p0Second.id, 30)(random, s.gameState.id)
      val healedGameState = damagedGameState.basicAttack(s.characters.p0First.id, s.characters.p0Second.id)

      healedGameState.characterById(s.characters.p0First.id).get
        .isFriendForC(s.characters.p0Second.id)(healedGameState) shouldBe true

      healedGameState.characterById(s.characters.p0Second.id).get
        .state.healthPoints should be(30 + healedGameState.characterById(s.characters.p0First.id).get.state.attackPoints)
    }
  }
}