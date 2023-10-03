package unit.abilities.sinon

import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.abilities.sinon.TacticalEscape
import com.tosware.nkm.models.game.character.{CharacterMetadata, StatType}
import helpers.{TestUtils, scenarios}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class TacticalEscapeSpec
    extends AnyWordSpecLike
    with Matchers
    with TestUtils {
  private val abilityMetadata = TacticalEscape.metadata
  private val metadata = CharacterMetadata.empty().copy(initialAbilitiesMetadataIds = Seq(abilityMetadata.id))
  private val s = scenarios.Simple2v2TestScenario(metadata)
  private val gameState: GameState = s.gameState
  private val abilityId = s.p(0)(0).character.state.abilities.head.id
  private val abilityUsedGameState: GameState = gameState.useAbility(abilityId)

  abilityMetadata.name must {
    "be able to use" in {
      assertCommandSuccess {
        GameStateValidator()(gameState)
          .validateAbilityUse(
            s.p(0)(0).ownerId,
            abilityId,
          )
      }
    }

    "be able to apply speed buff" in {
      assertBuffExists(StatType.Speed, s.p(0)(0).character.id)(abilityUsedGameState)
    }
  }
}
