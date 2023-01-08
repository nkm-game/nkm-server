package unit.abilities.ebisuzawa_kurumi

import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.abilities.ebisuzawa_kurumi.Survivor
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
  private val abilityId = s.characters.p0First.state.abilities.head.id

  abilityMetadata.name must {
    "apply basic attack buffs" in {
      val ngs: GameState = gameState.useAbilityWithoutTarget(abilityId)
      ngs.characterById(s.characters.p0First.id).state.effects.ofType[NextBasicAttackBuff] should not be empty
      ngs.basicAttack(s.characters.p0First.id, s.characters.p1First.id)
        .characterById(s.characters.p1First.id).state.effects.ofType[effects.Stun].size should be > 0
    }

    "become invisible" in {
      val ngs: GameState = gameState.useAbilityWithoutTarget(abilityId)
      val character = ngs.characterById(s.characters.p0First.id)

      val p0CharacterView = character.toView(Some(s.characters.p0First.owner.id))(ngs)
      val p1CharacterView = character.toView(Some(s.characters.p1First.owner.id))(ngs)

      p0CharacterView.state should not be None
      p1CharacterView.state should be (None)

      val p0MapView = ngs.hexMap.toView(Some(s.characters.p0First.owner.id))(ngs)
      val p1MapView = ngs.hexMap.toView(Some(s.characters.p1First.owner.id))(ngs)

      p0MapView.getCellOfCharacter(s.characters.p0First.id) should not be None
      p1MapView.getCellOfCharacter(s.characters.p0First.id) should be (None)
    }
  }
}