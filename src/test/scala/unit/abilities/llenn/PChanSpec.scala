package unit.abilities.llenn

import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.abilities.llenn.PChan
import com.tosware.nkm.models.game.character.CharacterMetadata
import com.tosware.nkm.models.game.game_state.GameState
import com.tosware.nkm.models.game.hex.TestHexMapName
import helpers.{TestScenario, TestUtils}

class PChanSpec extends TestUtils {
  private val metadata = CharacterMetadata.empty().copy(initialAbilitiesMetadataIds = Seq(PChan.metadata.id))
  private val s = TestScenario.generate(TestHexMapName.Simple2v2, metadata)
  implicit private val gameState: GameState = s.gameState

  PChan.metadata.name must {
    "increase movement speed on death of friend" in {
      val initialSpeed = metadata.initialSpeed
      val newGameState =
        gameState.damageCharacter(s.p(1)(1).character.id, Damage(DamageType.True, 100))(random, gameState.id)
      newGameState.characterById(s.defaultCharacter.id).state.pureSpeed should be(initialSpeed)
      val newGameState2 =
        gameState.damageCharacter(s.p(0)(1).character.id, Damage(DamageType.True, 100))(random, gameState.id)
      newGameState2.characterById(s.defaultCharacter.id).state.pureSpeed should be(initialSpeed + 2)
    }
  }
}
