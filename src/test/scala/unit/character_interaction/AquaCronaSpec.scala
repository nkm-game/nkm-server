package unit.character_interaction

import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game.ability.UseData
import com.tosware.nkm.models.game.game_state.GameState
import com.tosware.nkm.models.game.hex.TestHexMapName
import com.tosware.nkm.providers.CharacterMetadataProvider
import helpers.{TestScenario, TestUtils}

class AquaCronaSpec extends TestUtils {
  private val characters = CharacterMetadataProvider()
    .getCharacterMetadataSeq.filter(c => Seq("Aqua", "Crona").contains(c.name)).reverse
  private val s = TestScenario.generateFromSeq(TestHexMapName.Simple1v1, characters)
  private val gameState: GameState = s.ultGs
  private val blackBloodId = s.defaultCharacter.state.abilities(2).id
  private val purificationId = s.defaultEnemy.state.abilities(1).id

  "Aqua with Crona" must {
    "purify Black Blood" in {
      val bbGameState = gameState.useAbility(blackBloodId, UseData(s.defaultEnemy.id)).endTurn()
      assertCommandSuccess {
        GameStateValidator()(bbGameState)
          .validateAbilityUse(s.owners(1), purificationId, UseData(s.defaultEnemy.id))

      }
      val purifiedGameState = bbGameState.useAbility(purificationId, UseData(s.defaultEnemy.id))
      purifiedGameState.characterById(s.defaultEnemy.id).state.effects.size should be(0)
    }
  }
}
