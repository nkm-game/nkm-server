package unit.abilities.shana

import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.abilities.shana.GreatBladeOfCrimson
import com.tosware.nkm.models.game.character.CharacterMetadata
import helpers.{TestUtils, scenarios}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class GreatBladeOfCrimsonSpec
  extends AnyWordSpecLike
    with Matchers
    with TestUtils
{
  private val abilityMetadata = GreatBladeOfCrimson.metadata
  private val metadata = CharacterMetadata.empty()
    .copy(initialAbilitiesMetadataIds = Seq(abilityMetadata.id))
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

    "increase character AD" in {
      val oldSpeed = s.p(0)(0).character.state.attackPoints
      val newSpeed = abilityUsedGameState.characterById(s.p(0)(0).character.id).state.attackPoints
      oldSpeed should be < newSpeed
    }

    "increase basic attack range" in {
      val oldRange = s.p(0)(0).character.state.basicAttackRange
      val newRange = abilityUsedGameState.characterById(s.p(0)(0).character.id).state.basicAttackRange
      oldRange should be < newRange
    }
  }
}