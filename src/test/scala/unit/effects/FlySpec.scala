package unit.effects

import com.tosware.nkm.*
import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.character.CharacterMetadata
import com.tosware.nkm.models.game.effects.Fly
import helpers.{TestUtils, scenarios}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class FlySpec
  extends AnyWordSpecLike
    with Matchers
    with TestUtils
{
  private val effectMetadata = Fly.metadata
  private val metadata = CharacterMetadata.empty()
  private val s = scenarios.FlyTestScenario(metadata)
  private implicit val gameState: GameState = s.gameState.addEffect(s.p(0)(0).character.id, Fly("test_id", 2))

  effectMetadata.name.toString must {
    "allow flying over walls" in {
      val r = GameStateValidator()
        .validateBasicMoveCharacter(
          s.p(0)(0).character.owner.id,
          CoordinateSeq((0, 0), (1, 0), (2, 0)),
          s.p(0)(0).character.id,
        )
      assertCommandSuccess(r)
    }
    "allow flying over enemy characters" in {
      val r = GameStateValidator()
        .validateBasicMoveCharacter(
          s.p(0)(0).character.owner.id,
          CoordinateSeq((0, 0), (1, 0), (2, 0), (3, 0), (4, 0)),
          s.p(0)(0).character.id,
        )
      assertCommandSuccess(r)
    }
  }
}