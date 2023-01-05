package unit.character_interaction

import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game._
import com.tosware.nkm.providers.CharacterMetadatasProvider
import helpers.{TestUtils, scenarios}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class AquaCronaSpec
  extends AnyWordSpecLike
    with Matchers
    with TestUtils
{
  private val characters = CharacterMetadatasProvider().getCharacterMetadatas
  private val s = scenarios.Simple1v1TestScenario(characters.find(_.name == "Crona").get, characters.find(_.name == "Aqua"))
  private implicit val gameState: GameState = s.gameState.incrementPhase(4)
  private val blackBloodId = s.characters.p0.state.abilities(2).id
  private val purificationId = s.characters.p1.state.abilities(1).id

  "Aqua with Crona" must {
    "purify Black Blood" in {
      val bbGameState = gameState.useAbilityOnCharacter(blackBloodId, s.characters.p1.id).endTurn()
      assertCommandSuccess {
        GameStateValidator()(bbGameState)
          .validateAbilityUseOnCharacter(s.characters.p1.owner.id, purificationId, s.characters.p1.id)

      }
      val purifiedGameState = bbGameState.useAbilityOnCharacter(purificationId, s.characters.p1.id)
      purifiedGameState.characterById(s.characters.p1.id).state.effects.size should be (0)
    }
  }
}