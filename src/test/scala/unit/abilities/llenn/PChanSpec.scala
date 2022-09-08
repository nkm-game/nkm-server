package unit.abilities.llenn

import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.abilities.llenn.PChan
import com.tosware.nkm.models.{Damage, DamageType}
import helpers.{TestUtils, scenarios}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class PChanSpec
  extends AnyWordSpecLike
    with Matchers
    with TestUtils
{
  private val metadata = CharacterMetadata.empty().copy(initialAbilitiesMetadataIds = Seq(PChan.metadata.id))
  private val s = scenarios.Simple2v2TestScenario(metadata)
  private implicit val gameState: GameState = s.gameState

  PChan.metadata.name must {
    "increase movement speed on death of friend" in {
      val initialSpeed = metadata.initialSpeed
      val newGameState = gameState.damageCharacter(s.characters.p1Second.id, Damage(DamageType.True, 100))(random, gameState.id)
      newGameState.characterById(s.characters.p0First.id).get.state.pureSpeed should be(initialSpeed)
      val newGameState2 = gameState.damageCharacter(s.characters.p0Second.id, Damage(DamageType.True, 100))(random, gameState.id)
      newGameState2.characterById(s.characters.p0First.id).get.state.pureSpeed should be(initialSpeed + 2)
    }
  }
}