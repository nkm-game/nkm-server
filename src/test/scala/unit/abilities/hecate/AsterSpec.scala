package unit.abilities.hecate

import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.abilities.hecate.Aster
import com.tosware.nkm.models.game.character.CharacterMetadata
import com.tosware.nkm.models.game.event.GameEvent
import helpers.{TestUtils, scenarios}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class AsterSpec
  extends AnyWordSpecLike
    with Matchers
    with TestUtils
{
  private val metadata = CharacterMetadata.empty().copy(initialAbilitiesMetadataIds = Seq(Aster.metadata.id))
  private val s = scenarios.Simple2v2TestScenario(metadata)
  private implicit val gameState: GameState = s.gameState
  private val abilityId = s.p(0)(0).character.state.abilities.head.id

  Aster.metadata.name must {
    "be able to use on all coords" in {
      val validator = GameStateValidator()

      val allCoords = s.gameState.hexMap.cells.toCoords
      allCoords.foreach { c =>
        val r = validator.validateAbilityUseOnCoordinates(s.p(0)(0).character.owner.id, abilityId, c)
        assertCommandSuccess(r)
      }
    }

    "be able to damage characters" in {
      val abilityUsedGameState: GameState = s.gameState.useAbilityOnCoordinates(abilityId, s.p(0)(1).spawnCoordinates)
      abilityUsedGameState.gameLog.events
        .ofType[GameEvent.CharacterDamaged]
        .causedBy(abilityId)
        .size should be (2)
    }
  }
}