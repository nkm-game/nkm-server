package unit.character_interaction

import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game.*
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
  private val blackBloodId = s.p(0)(0).character.state.abilities(2).id
  private val purificationId = s.p(1)(0).character.state.abilities(1).id

  "Aqua with Crona" must {
    "purify Black Blood" in {
      val bbGameState = gameState.useAbilityOnCharacter(blackBloodId, s.p(1)(0).character.id).endTurn()
      assertCommandSuccess {
        GameStateValidator()(bbGameState)
          .validateAbilityUseOnCharacter(s.p(1)(0).character.owner.id, purificationId, s.p(1)(0).character.id)

      }
      val purifiedGameState = bbGameState.useAbilityOnCharacter(purificationId, s.p(1)(0).character.id)
      purifiedGameState.characterById(s.p(1)(0).character.id).state.effects.size should be (0)
    }
  }
}