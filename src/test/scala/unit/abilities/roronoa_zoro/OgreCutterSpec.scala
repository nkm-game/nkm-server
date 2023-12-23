package unit.abilities.roronoa_zoro

import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.abilities.roronoa_zoro.OgreCutter
import com.tosware.nkm.models.game.ability.UseData
import com.tosware.nkm.models.game.event.GameEvent
import com.tosware.nkm.models.game.hex.{HexCoordinates, TestHexMapName}
import helpers.{TestScenario, TestUtils}

class OgreCutterSpec extends TestUtils {

  private val abilityMetadata = OgreCutter.metadata
  private val s = TestScenario.generate(TestHexMapName.OgreCutter, abilityMetadata.id)
  private val abilityId = s.defaultAbilityId

  abilityMetadata.name must {
    "be able to use" in {
      assertCommandSuccess {
        GameStateValidator()(s.gameState)
          .validateAbilityUse(s.owners(0), abilityId, UseData(s.defaultEnemy.id))
      }
    }

    "not be able to use if teleport cell is not free to stand" in {
      val newGameState = s.gameState.teleportCharacter(s.defaultEnemy.id, HexCoordinates(4, 0))
      val targetsInRange = newGameState.abilityById(abilityId).targetsInRange(newGameState)
      val targetCoordinates = s.defaultEnemy.parentCellOpt(newGameState).get.coordinates

      targetsInRange should not contain targetCoordinates

      assertCommandFailure {
        GameStateValidator()(newGameState)
          .validateAbilityUse(s.owners(0), abilityId, UseData(s.defaultEnemy.id))
      }
    }

    "not be able to use if teleport cell does not exist" in {
      val newGameState = s.gameState.teleportCharacter(s.defaultEnemy.id, HexCoordinates(5, 0))
      assertCommandFailure {
        GameStateValidator()(newGameState)
          .validateAbilityUse(s.owners(0), abilityId, UseData(s.defaultEnemy.id))
      }
    }

    "be able to damage and teleport" in {
      val newGameState: GameState = s.gameState.useAbility(abilityId, UseData(s.defaultEnemy.id))

      newGameState
        .gameLog.events
        .causedBy(s.defaultCharacter.id)
        .ofType[GameEvent.CharacterDamaged] should not be empty

      newGameState.hexMap
        .getCellOfCharacter(s.defaultCharacter.id).get
        .coordinates.toTuple shouldBe (5, 0)
    }
  }
}
