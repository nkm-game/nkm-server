package unit.abilities.ebisuzawa_kurumi

import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.abilities.ebisuzawa_kurumi.Survivor
import com.tosware.nkm.models.game.character.CharacterMetadata
import com.tosware.nkm.models.game.effects.NextBasicAttackBuff
import helpers.{TestUtils, scenarios}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class SurvivorSpec
  extends AnyWordSpecLike
    with Matchers
    with TestUtils
{
  private val abilityMetadata = Survivor.metadata
  private val characterMetadata = CharacterMetadata.empty().copy(initialAbilitiesMetadataIds = Seq(abilityMetadata.id))
  private val s = scenarios.Simple2v2TestScenario(characterMetadata)
  private implicit val gameState: GameState = s.gameState
  private val abilityId = s.p(0)(0).character.state.abilities.head.id

  abilityMetadata.name must {
    "apply basic attack buffs" in {
      val ngs: GameState = gameState.useAbility(abilityId)
      ngs.characterById(s.p(0)(0).character.id).state.effects.ofType[NextBasicAttackBuff] should not be empty
      val attackGs = ngs.basicAttack(s.p(0)(0).character.id, s.p(1)(0).character.id)
      attackGs.characterById(s.p(1)(0).character.id).state.effects.ofType[effects.Stun].size should be > 0
      attackGs.characterById(s.p(1)(0).character.id).isDead shouldBe false
    }

    "become invisible" in {
      val ngs: GameState = gameState.useAbility(abilityId)
      val character = ngs.characterById(s.p(0)(0).character.id)

      val p0CharacterView = character.toView(Some(s.p(0)(0).character.owner.id))(ngs)
      val p1CharacterView = character.toView(Some(s.p(1)(0).character.owner.id))(ngs)

      p0CharacterView.state should not be None
      p1CharacterView.state should be (None)

      val p0MapView = ngs.hexMap.toView(Some(s.p(0)(0).character.owner.id))(ngs)
      val p1MapView = ngs.hexMap.toView(Some(s.p(1)(0).character.owner.id))(ngs)

      p0MapView.getCellOfCharacter(s.p(0)(0).character.id) should not be None
      p1MapView.getCellOfCharacter(s.p(0)(0).character.id) should be (None)
    }
  }
}