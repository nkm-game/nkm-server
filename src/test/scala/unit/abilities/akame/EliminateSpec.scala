package unit.abilities.akame

import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.abilities.akame.Eliminate
import com.tosware.nkm.models.game.character.CharacterMetadata
import com.tosware.nkm.models.game.event.GameEvent.CharacterDamaged
import helpers.{TestUtils, scenarios}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class EliminateSpec
  extends AnyWordSpecLike
    with Matchers
    with TestUtils
{
  private val abilityMetadata = Eliminate.metadata
  private val characterMetadata = CharacterMetadata.empty()
    .copy(initialAbilitiesMetadataIds = Seq(abilityMetadata.id))
  private val s = scenarios.Simple1v1TestScenario(characterMetadata)
  private implicit val gameState: GameState = s.gameState
  private val abilityId = s.p(0)(0).character.state.abilities.head.id

  abilityMetadata.name must {
    "be able to use" in {
      val r = GameStateValidator()
        .validateAbilityUseOnCharacter(s.p(0)(0).character.owner.id, abilityId, s.p(1)(0).character.id)
      assertCommandSuccess(r)
    }
    "deal damage" in {
      val newGameState: GameState = gameState.useAbilityOnCharacter(
        abilityId,
        s.p(1)(0).character.id
      )

      newGameState
        .gameLog
        .events
        .causedBy(abilityId)
        .ofType[CharacterDamaged] should not be empty
    }
  }
}