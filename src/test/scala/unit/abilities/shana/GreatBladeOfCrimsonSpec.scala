package unit.abilities.shana

import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.abilities.shana.GreatBladeOfCrimson
import com.tosware.nkm.models.game.character.{CharacterMetadata, StatType}
import com.tosware.nkm.models.game.hex.TestHexMapName
import helpers.{TestScenario, TestUtils}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class GreatBladeOfCrimsonSpec
    extends AnyWordSpecLike
    with Matchers
    with TestUtils {
  private val abilityMetadata = GreatBladeOfCrimson.metadata
  private val metadata = CharacterMetadata.empty()
    .copy(initialAbilitiesMetadataIds = Seq(abilityMetadata.id))
  private val s = TestScenario.generate(TestHexMapName.Simple2v2, metadata)
  private val gameState: GameState = s.gameState
  private val abilityId = s.defaultAbilityId
  private val abilityUsedGameState: GameState = gameState.useAbility(abilityId)

  abilityMetadata.name must {
    "be able to use" in {
      assertCommandSuccess {
        GameStateValidator()(gameState)
          .validateAbilityUse(
            s.owners(0),
            abilityId,
          )
      }
    }

    "apply AD buff" in {
      assertBuffExists(StatType.AttackPoints, s.defaultCharacter.id)(abilityUsedGameState)
    }

    "apply basic attack range buff" in {
      assertBuffExists(StatType.BasicAttackRange, s.defaultCharacter.id)(abilityUsedGameState)
    }
  }
}
