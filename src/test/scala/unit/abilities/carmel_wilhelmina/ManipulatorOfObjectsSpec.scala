package unit.abilities.carmel_wilhelmina

import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.abilities.carmel_wilhelmina.ManipulatorOfObjects
import com.tosware.nkm.models.game.effects.Snare
import com.tosware.nkm.NkmUtils.SeqUtils
import helpers.{TestUtils, scenarios}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class ManipulatorOfObjectsSpec
  extends AnyWordSpecLike
    with Matchers
    with TestUtils
{

  private val abilityMetadata = ManipulatorOfObjects.metadata
  private val characterMetadata = CharacterMetadata.empty().copy(initialAbilitiesMetadataIds = Seq(abilityMetadata.id))
  private val s = scenarios.Simple1v1TestScenario(characterMetadata)
  private implicit val gameState: GameState = s.gameState

  abilityMetadata.name must {
    "root enemies" in {
      val attackedGameState = gameState.basicAttack(s.characters.p0.id, s.characters.p1.id)
      attackedGameState.characterById(s.characters.p1.id).get.state.effects.ofType[Snare] should not be empty
    }
    "disallow rooting enemies in another phase" in {
      val attackedSecondTimeGameState = gameState
        .basicAttack(s.characters.p0.id, s.characters.p1.id)
        .endTurn()
        .passTurn(s.characters.p1.id)
        .basicAttack(s.characters.p0.id, s.characters.p1.id)

      attackedSecondTimeGameState.characterById(s.characters.p1.id).get.state.effects.ofType[Snare] should be (empty)
    }
  }
}