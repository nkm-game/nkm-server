package unit.abilities.ryuko_matoi

import com.tosware.nkm.models.game.abilities.ryuko_matoi.ScissorBlade
import com.tosware.nkm.models.game.character.{CharacterMetadata, StatType}
import com.tosware.nkm.models.game.effects.StatNerf
import com.tosware.nkm.models.game.game_state.GameState
import com.tosware.nkm.models.game.hex.TestHexMapName
import helpers.{TestScenario, TestUtils}

class ScissorBladeSpec extends TestUtils {

  private val abilityMetadata = ScissorBlade.metadata
  private val characterMetadata = CharacterMetadata.empty().copy(initialAbilitiesMetadataIds = Seq(abilityMetadata.id))
  private val s = TestScenario.generate(TestHexMapName.Simple1v1, characterMetadata)
  implicit private val gameState: GameState = s.gameState

  abilityMetadata.name must {
    "decrease target physical defense" in {
      val attackedGameState = gameState.basicAttack(s.defaultCharacter.id, s.defaultEnemy.id)
      val statNerfEffects = attackedGameState.characterById(s.defaultEnemy.id).state.effects.ofType[StatNerf]
      statNerfEffects should not be empty
      statNerfEffects.head.statType should be(StatType.PhysicalDefense)
    }
  }
}
