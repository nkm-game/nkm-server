package unit.abilities.sinon

import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game.abilities.sinon.SnipersSight
import com.tosware.nkm.models.game.character.CharacterMetadata
import com.tosware.nkm.models.game.game_state.GameState
import com.tosware.nkm.models.game.hex.{HexCoordinates, TestHexMapName}
import helpers.{TestScenario, TestUtils}

class SnipersSightSpec extends TestUtils {
  private val abilityMetadata = SnipersSight.metadata
  private val metadata = CharacterMetadata.empty().copy(initialAbilitiesMetadataIds = Seq(abilityMetadata.id))
  private val s = TestScenario.generate(TestHexMapName.Simple2v2, metadata)
  private val gameState: GameState = s.gameState

  abilityMetadata.name must {
    "be able to attack characters in radial range" in {
      val ngs = gameState.teleportCharacter(s.defaultCharacter.id, HexCoordinates(0, 1))(random, gameState.id)
      assertCommandSuccess {
        GameStateValidator()(ngs)
          .validateBasicAttackCharacter(s.owners(0), s.defaultCharacter.id, s.p(1)(1).character.id)
      }
    }
    "not contain parent in range" in {
      gameState.characterById(s.defaultCharacter.id)
        .basicAttackCellCoords(gameState) should not contain s.defaultCoordinates
    }
  }
}
