package unit.abilities.dekomori_sanae

import com.tosware.nkm.*
import com.softwaremill.quicklens.*
import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.abilities.dekomori_sanae.MjolnirDestinyImpulse
import com.tosware.nkm.models.game.event.GameEvent
import com.tosware.nkm.models.game.hex.TestHexMapName
import helpers.{TestScenario, TestUtils}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class MjolnirDestinyImpulseSpec
    extends AnyWordSpecLike
    with Matchers
    with TestUtils {
  private val abilityMetadata = MjolnirDestinyImpulse.metadata
  private val s = TestScenario.generate(TestHexMapName.Simple2v2v2, abilityMetadata.id)

  private val aGsNoRefresh0: GameState =
    s.ultGs
      .useAbilityOnCoordinates(s.defaultAbilityId, s.p(1)(0).spawnCoordinates)

  private val aGsRefresh1: GameState =
    s.ultGs
      .updateCharacter(s.p(1)(0).character.id)(_.modify(_.state.healthPoints).setTo(1))
      .useAbilityOnCoordinates(s.defaultAbilityId, s.p(1)(0).spawnCoordinates)

  private val aGsNoRefresh1: GameState =
    aGsRefresh1
      .useAbilityOnCoordinates(s.defaultAbilityId, s.p(1)(0).spawnCoordinates)

  private val aGsRefresh2: GameState =
    aGsRefresh1
      .updateCharacter(s.p(1)(1).character.id)(_.modify(_.state.healthPoints).setTo(1))
      .useAbilityOnCoordinates(s.defaultAbilityId, s.p(1)(0).spawnCoordinates)

  private val aGsNoRefresh2: GameState =
    aGsRefresh2
      .useAbilityOnCoordinates(s.defaultAbilityId, s.p(1)(0).spawnCoordinates)

  abilityMetadata.name must {
    "be able to use on all coords" in {
      val validator = GameStateValidator()(s.ultGs)
      val allCoords = s.gameState.hexMap.cells.map(_.coordinates)
      allCoords.foreach { c =>
        assertCommandSuccess {
          validator.validateAbilityUseOnCoordinates(s.owners(0), s.defaultAbilityId, c)
        }
      }
    }

    "be able to damage characters" in {
      aGsNoRefresh0.gameLog.events
        .ofType[GameEvent.CharacterDamaged]
        .causedBy(s.defaultAbilityId)
        .size should be(3)
    }

    "not be able to use again if no enemies were killed" in {
      assertCommandFailure {
        GameStateValidator()(aGsNoRefresh0)
          .validateAbilityUseOnCoordinates(s.owners(0), s.defaultAbilityId, s.p(1)(0).spawnCoordinates)
      }
      assertCommandFailure {
        GameStateValidator()(aGsNoRefresh1)
          .validateAbilityUseOnCoordinates(s.owners(0), s.defaultAbilityId, s.p(1)(0).spawnCoordinates)
      }
      assertCommandFailure {
        GameStateValidator()(aGsNoRefresh2)
          .validateAbilityUseOnCoordinates(s.owners(0), s.defaultAbilityId, s.p(1)(0).spawnCoordinates)
      }
    }
    "be able to use again if an enemy was killed by ability" in {
      assertCommandSuccess {
        GameStateValidator()(aGsRefresh1)
          .validateAbilityUseOnCoordinates(s.owners(0), s.defaultAbilityId, s.p(1)(0).spawnCoordinates)
      }
      assertCommandSuccess {
        GameStateValidator()(aGsRefresh2)
          .validateAbilityUseOnCoordinates(s.owners(0), s.defaultAbilityId, s.p(1)(0).spawnCoordinates)
      }
    }
  }
}
