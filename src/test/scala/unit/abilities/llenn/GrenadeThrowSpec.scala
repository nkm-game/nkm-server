package unit.abilities.llenn

import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game.abilities.llenn.GrenadeThrow
import com.tosware.nkm.models.game.ability.UseData
import com.tosware.nkm.models.game.event.GameEvent
import com.tosware.nkm.models.game.game_state.GameState
import com.tosware.nkm.models.game.hex.{HexCoordinates, TestHexMapName}
import helpers.{TestScenario, TestUtils}
import org.scalatest.wordspec.AnyWordSpecLike

class GrenadeThrowSpec
    extends AnyWordSpecLike
    with TestUtils {
  private val abilityMetadata = GrenadeThrow.metadata
  private val s = TestScenario.generate(TestHexMapName.Simple2v2, abilityMetadata.id)
  private val aGs: GameState = s.gameState.useAbility(s.defaultAbilityId, UseData(HexCoordinates(2, 0)))

  abilityMetadata.name must {
    "be able to use on all coords" in {
      val validator = GameStateValidator()(s.gameState)
      val allCoords = s.gameState.hexMap.cells.map(_.coordinates)
      allCoords.foreach { c =>
        assertCommandSuccess {
          validator.validateAbilityUse(s.owners(0), s.defaultAbilityId, UseData(c))
        }
      }
    }

    "be able to damage characters" in {
      aGs.gameLog.events
        .ofType[GameEvent.CharacterDamaged]
        .causedBy(s.defaultAbilityId)
        .size should be(3)
    }
  }
}
