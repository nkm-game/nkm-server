package unit.abilities.carmel_wilhelmina

import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.abilities.carmel_wilhelmina.ManipulatorOfObjects
import com.tosware.nkm.models.game.character.CharacterMetadata
import com.tosware.nkm.models.game.effects.Snare
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
      val attackedGameState = gameState.basicAttack(s.p(0)(0).character.id, s.p(1)(0).character.id)
      attackedGameState.characterById(s.p(1)(0).character.id).state.effects.ofType[Snare] should not be empty
    }
    "disallow rooting enemies in another phase" in {
      val attackedSecondTimeGameState = gameState
        .basicAttack(s.p(0)(0).character.id, s.p(1)(0).character.id)
        .endTurn()
        .passTurn(s.p(1)(0).character.id)
        .basicAttack(s.p(0)(0).character.id, s.p(1)(0).character.id)

      attackedSecondTimeGameState.characterById(s.p(1)(0).character.id).state.effects.ofType[Snare] should be (empty)
    }
  }
}