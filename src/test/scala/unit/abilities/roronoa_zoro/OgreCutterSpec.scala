package unit.abilities.roronoa_zoro

import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.abilities.roronoa_zoro.OgreCutter
import com.tosware.nkm.models.game.character.CharacterMetadata
import com.tosware.nkm.models.game.event.GameEvent
import com.tosware.nkm.models.game.hex.HexCoordinates
import helpers.{TestUtils, scenarios}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class OgreCutterSpec
  extends AnyWordSpecLike
    with Matchers
    with TestUtils
{

  private val abilityMetadata = OgreCutter.metadata
  private val metadata = CharacterMetadata.empty().copy(initialAbilitiesMetadataIds = Seq(abilityMetadata.id))
  private val s = scenarios.OgreCutterTestScenario(metadata)
  private implicit val gameState: GameState = s.gameState
  private val abilityId = s.p(0)(0).character.state.abilities.head.id

  abilityMetadata.name must {
    "be able to use" in {
      assertCommandSuccess {
        GameStateValidator()
          .validateAbilityUseOnCharacter(s.p(0)(0).character.owner.id, abilityId, s.p(1)(0).character.id)
      }
    }

    "not be able to use if teleport cell is not free to stand" in {
      val newGameState = gameState.teleportCharacter(s.p(1)(0).character.id, HexCoordinates(4, 0))
      assertCommandFailure {
        GameStateValidator()(newGameState)
          .validateAbilityUseOnCharacter(s.p(0)(0).character.owner.id, abilityId, s.p(1)(0).character.id)
      }
    }

    "not be able to use if teleport cell does not exist" in {
      val newGameState = gameState.teleportCharacter(s.p(1)(0).character.id, HexCoordinates(5, 0))
      assertCommandFailure {
        GameStateValidator()(newGameState)
          .validateAbilityUseOnCharacter(s.p(0)(0).character.owner.id, abilityId, s.p(1)(0).character.id)
      }
    }

    "be able to damage and teleport" in {
      val newGameState: GameState = gameState.useAbilityOnCharacter(abilityId, s.p(1)(0).character.id)

      newGameState
        .gameLog.events
        .causedBy(s.p(0)(0).character.id)
        .ofType[GameEvent.CharacterDamaged] should not be empty

      newGameState.hexMap
        .getCellOfCharacter(s.p(0)(0).character.id).get
        .coordinates.toTuple shouldBe (5, 0)
    }
  }
}