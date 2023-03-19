package unit.abilities.llenn

import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.abilities.llenn.GrenadeThrow
import com.tosware.nkm.models.game.event.GameEvent
import com.tosware.nkm.models.game.hex.{HexCoordinates, TestHexMapName}
import helpers.{TestScenario, TestUtils}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class GrenadeThrowSpec
  extends AnyWordSpecLike
    with Matchers
    with TestUtils
{
  private val abilityMetadata = GrenadeThrow.metadata
  private val s = TestScenario.generate(TestHexMapName.Simple2v2, abilityMetadata.id)
  private val aGs: GameState = s.gameState.useAbilityOnCoordinates(s.defaultAbilityId, HexCoordinates(2, 0))

  abilityMetadata.name must {
    "be able to use on all coords" in {
      val validator = GameStateValidator()(s.gameState)
      val allCoords = s.gameState.hexMap.cells.map(_.coordinates)
      allCoords.foreach { c =>
        assertCommandSuccess {
          validator.validateAbilityUseOnCoordinates(s.owners(0), s.defaultAbilityId, c)
        }
      }
    }

    "be able to damage characters" in {
      aGs.gameLog.events
        .ofType[GameEvent.CharacterDamaged]
        .causedBy(s.defaultAbilityId)
        .size should be (3)
    }
  }
}