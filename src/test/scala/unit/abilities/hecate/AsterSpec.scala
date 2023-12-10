package unit.abilities.hecate

import com.tosware.nkm.*
import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.abilities.hecate.Aster
import com.tosware.nkm.models.game.ability.UseData
import com.tosware.nkm.models.game.event.GameEvent
import com.tosware.nkm.models.game.hex.TestHexMapName
import helpers.{TestScenario, TestUtils}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class AsterSpec
    extends AnyWordSpecLike
    with Matchers
    with TestUtils {
  private val abilityMetadata = Aster.metadata
  private val s = TestScenario.generate(TestHexMapName.Simple2v2, abilityMetadata.id)

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
      val abilityUsedGameState: GameState =
        s.gameState.useAbility(s.defaultAbilityId, UseData(s.p(0)(1).spawnCoordinates))
      abilityUsedGameState.gameLog.events
        .ofType[GameEvent.CharacterDamaged]
        .causedBy(s.defaultAbilityId)
        .size should be(2)
    }
  }
}
