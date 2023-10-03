package unit.abilities.sinon

import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.abilities.sinon.SnipersSight
import com.tosware.nkm.models.game.character.CharacterMetadata
import com.tosware.nkm.models.game.hex.HexCoordinates
import helpers.{TestUtils, scenarios}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class SnipersSightSpec
    extends AnyWordSpecLike
    with Matchers
    with TestUtils {
  private val abilityMetadata = SnipersSight.metadata
  private val metadata = CharacterMetadata.empty().copy(initialAbilitiesMetadataIds = Seq(abilityMetadata.id))
  private val s = scenarios.Simple2v2TestScenario(metadata)
  private val gameState: GameState = s.gameState

  abilityMetadata.name must {
    "be able to attack characters in radial range" in {
      val ngs = gameState.teleportCharacter(s.p(0)(0).character.id, HexCoordinates(0, 1))(random, gameState.id)
      assertCommandSuccess {
        GameStateValidator()(ngs)
          .validateBasicAttackCharacter(s.p(0)(0).ownerId, s.p(0)(0).character.id, s.p(1)(1).character.id)
      }
    }
  }
}
