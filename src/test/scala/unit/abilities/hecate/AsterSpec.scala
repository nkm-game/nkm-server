package unit.abilities.hecate

import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.abilities.hecate.Aster
import com.tosware.nkm.models.game.hex.HexUtils._
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
  private val abilityId = s.characters.p0First.state.abilities.head.id

  Aster.metadata.name must {
    "be able to damage characters" in {
      val validator = GameStateValidator()

      val allCoords = s.gameState.hexMap.get.cells.toCoords
      allCoords.foreach { c =>
        val r = validator.validateAbilityUseOnCoordinates(s.characters.p0First.owner.id, abilityId, c)
        assertCommandSuccess(r)
      }

      val abilityUsedGameState: GameState = s.gameState.useAbilityOnCoordinates(abilityId, s.spawnCoordinates.p0Second)
      abilityUsedGameState.gameLog.events
        .ofType[GameEvent.CharacterDamaged]
        .causedBy(abilityId)
        .size should be (2)
    }
  }
}