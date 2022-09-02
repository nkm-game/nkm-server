package unit.abilities.llenn

import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.abilities.llenn.GrenadeThrow
import com.tosware.nkm.models.game.hex.HexCoordinates
import com.tosware.nkm.models.game.hex.HexUtils._
import helpers.{Simple2v2TestScenario, TestUtils}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class GrenadeThrowSpec
  extends AnyWordSpecLike
    with Matchers
    with TestUtils
{
  private val metadata = CharacterMetadata.empty().copy(initialAbilitiesMetadataIds = Seq(GrenadeThrow.metadata.id))
  private val s = Simple2v2TestScenario(metadata)
  private implicit val gameState: GameState = s.gameState
  private val abilityId = s.characters.p0First.state.abilities.head.id

  GrenadeThrow.metadata.name must {
    "be able to damage characters" in {
      val validator = GameStateValidator()

      val allCoords = gameState.hexMap.get.cells.toCoords
      allCoords.foreach { c =>
        val r = validator.validateAbilityUseOnCoordinates(s.characters.p0First.owner.id, abilityId, c)
        assertCommandSuccess(r)
      }

      val abilityUsedGameState: GameState = gameState.useAbilityOnCoordinates(abilityId, HexCoordinates(2, 0))
      abilityUsedGameState.gameLog.events
        .ofType[GameEvent.CharacterDamaged]
        .causedBy(abilityId)
        .size should be (3)
    }
  }
}