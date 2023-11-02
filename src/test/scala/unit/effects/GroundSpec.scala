package unit.effects

import com.tosware.nkm.*
import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.abilities.kirito.Switch
import com.tosware.nkm.models.game.abilities.sinon.TacticalEscape
import com.tosware.nkm.models.game.character.CharacterMetadata
import com.tosware.nkm.models.game.effects.Ground
import com.tosware.nkm.models.game.hex.TestHexMapName
import helpers.{TestScenario, TestUtils}
import org.scalatest.wordspec.AnyWordSpecLike

class GroundSpec
    extends AnyWordSpecLike
    with TestUtils {
  private val effectMetadata = Ground.metadata
  private val characterMetadata =
    CharacterMetadata
      .empty()
      .copy(initialAbilitiesMetadataIds = Seq(Switch.metadata.id, TacticalEscape.metadata.id))
  private val s = TestScenario.generate(TestHexMapName.Simple2v2, characterMetadata)
  private val eGs: GameState = s.gameState.addEffect(s.defaultCharacter.id, Ground(randomUUID(), 2))
  private val defaultValidator = GameStateValidator()(eGs)

  effectMetadata.name.toString must {
    "disallow moving" in {
      assertCommandFailure {
        defaultValidator.validateBasicMoveCharacter(
          s.owners(0),
          CoordinateSeq((0, 0), (1, 0)),
          s.defaultCharacter.id,
        )
      }
    }
    "disallow using move abilities" in {
      assertCommandFailure {
        defaultValidator.validateAbilityUseOnCharacter(
          s.owners(0),
          s.defaultAbilityId,
          s.p(0)(1).character.id,
        )
      }
    }
    "allow using non-move abilities" in {
      assertCommandSuccess {
        defaultValidator.validateAbilityUse(
          s.owners(0),
          s.defaultCharacter.state.abilities(1).id,
        )
      }
    }
  }
}
