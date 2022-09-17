package unit.abilities.akame

import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game.GameEvent.CharacterDamaged
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.abilities.akame.Eliminate
import com.tosware.nkm.models.game.hex.HexUtils._
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
  private val abilityId = s.characters.p0.state.abilities.head.id

  abilityMetadata.name must {
    "be able to use" in {
      val r = GameStateValidator()
        .validateAbilityUseOnCharacter(s.characters.p0.owner.id, abilityId, s.characters.p1.id)
      assertCommandSuccess(r)
    }
    "deal damage" in {
      val newGameState: GameState = gameState.useAbilityOnCharacter(
        abilityId,
        s.characters.p1.id
      )

      newGameState
        .gameLog
        .events
        .causedBy(abilityId)
        .ofType[CharacterDamaged] should not be empty
    }
  }
}