package unit.effects

import com.tosware.nkm.*
import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game.effects.Fly
import com.tosware.nkm.models.game.game_state.GameState
import com.tosware.nkm.models.game.hex.TestHexMapName
import helpers.{TestScenario, TestUtils}
import org.scalatest.wordspec.AnyWordSpecLike

class FlySpec
    extends AnyWordSpecLike
    with TestUtils {
  private val effectMetadata = Fly.metadata
  private val s = TestScenario.generate(TestHexMapName.Fly)
  private val eGs: GameState = s.gameState.addEffect(s.defaultCharacter.id, Fly(randomUUID(), 2))

  effectMetadata.name.toString must {
    "allow flying over walls" in {
      val validator = GameStateValidator()(eGs)
      assertCommandSuccess {
        validator.validateBasicMoveCharacter(
          s.owners(0),
          CoordinateSeq((0, 0), (1, 0), (2, 0)),
          s.defaultCharacter.id,
        )
      }
    }
    "allow flying over enemy characters" in {
      val validator = GameStateValidator()(eGs)
      assertCommandSuccess {
        validator.validateBasicMoveCharacter(
          s.owners(0),
          CoordinateSeq((0, 0), (1, 0), (2, 0), (3, 0), (4, 0)),
          s.defaultCharacter.id,
        )
      }
    }
  }
}
