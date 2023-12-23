package unit.abilities.carmel_wilhelmina

import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.abilities.carmel_wilhelmina.ManipulatorOfObjects
import com.tosware.nkm.models.game.effects.Snare
import com.tosware.nkm.models.game.hex.TestHexMapName
import helpers.{TestScenario, TestUtils}

class ManipulatorOfObjectsSpec
    extends TestUtils {

  private val abilityMetadata = ManipulatorOfObjects.metadata
  private val s = TestScenario.generate(TestHexMapName.Simple1v1, abilityMetadata.id)
  implicit private val gameState: GameState = s.gameState

  abilityMetadata.name must {
    "snare enemies" in {
      val attackedGameState = gameState.basicAttack(s.defaultCharacter.id, s.defaultEnemy.id)
      attackedGameState.characterById(s.defaultEnemy.id).state.effects.ofType[Snare] should not be empty
    }
    "disallow snaring enemies in another phase" in {
      val attackedSecondTimeGameState = gameState
        .basicAttack(s.defaultCharacter.id, s.defaultEnemy.id)
        .endTurn()
        .passTurn(s.defaultEnemy.id)
        .basicAttack(s.defaultCharacter.id, s.defaultEnemy.id)

      attackedSecondTimeGameState.characterById(s.defaultEnemy.id).state.effects.ofType[Snare] should be(empty)
    }
  }
}
