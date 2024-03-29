package unit.abilities.ryuko_matoi

import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game.abilities.ryuko_matoi.{FiberDecapitation, ScissorBlade}
import com.tosware.nkm.models.game.ability.UseData
import com.tosware.nkm.models.game.character.CharacterMetadata
import com.tosware.nkm.models.game.effects.StatNerf
import com.tosware.nkm.models.game.event.GameEvent
import com.tosware.nkm.models.game.game_state.GameState
import com.tosware.nkm.models.game.hex.{HexCoordinates, TestHexMapName}
import helpers.{TestScenario, TestUtils}

class FiberDecapitationSpec extends TestUtils {
  private val abilityMetadata = FiberDecapitation.metadata
  private val characterMetadata =
    CharacterMetadata.empty()
      .copy(initialAbilitiesMetadataIds = Seq(abilityMetadata.id, ScissorBlade.metadata.id))
  private val s = TestScenario.generate(TestHexMapName.FiberDecapitation, characterMetadata)

  abilityMetadata.name must {
    "be able to use" in {
      assertCommandSuccess {
        GameStateValidator()(s.gameState)
          .validateAbilityUse(s.owners(0), s.defaultAbilityId, UseData(s.defaultEnemy.id))
      }
    }

    "not be able to use if teleport cell is not free to stand" in {
      val nGs = s.gameState.teleportCharacter(s.defaultEnemy.id, HexCoordinates(4, 0))
      assertCommandFailure {
        GameStateValidator()(nGs)
          .validateAbilityUse(s.owners(0), s.defaultAbilityId, UseData(s.defaultEnemy.id))
      }
    }

    "not be able to use if teleport cell does not exist" in {
      val nGs = s.gameState.teleportCharacter(s.defaultEnemy.id, HexCoordinates(5, 0))
      assertCommandFailure {
        GameStateValidator()(nGs)
          .validateAbilityUse(s.owners(0), s.defaultAbilityId, UseData(s.defaultEnemy.id))
      }
    }

    "be able to decrease physical defense, damage and teleport" in {
      val oldPhysicalDefense = s.gameState.characterById(s.defaultEnemy.id).state.purePhysicalDefense

      val newGameState: GameState = s.gameState.useAbility(s.defaultAbilityId, UseData(s.defaultEnemy.id))
      val newPhysicalDefense = newGameState.characterById(s.defaultEnemy.id).state.purePhysicalDefense

      oldPhysicalDefense should be > newPhysicalDefense

      newGameState
        .gameLog.events
        .causedBy(s.defaultAbilityId)
        .ofType[GameEvent.CharacterDamaged] should not be empty

      newGameState.hexMap
        .getCellOfCharacter(s.defaultCharacter.id).get
        .coordinates.toTuple shouldBe (6, 0)
    }

    "not apply basic attack effects" in {
      val aGs: GameState = s.gameState.useAbility(s.defaultAbilityId, UseData(s.defaultEnemy.id))
      assertEffectDoesNotExistOfType[StatNerf](s.defaultEnemy.id)(aGs)
    }
  }
}
