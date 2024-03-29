package unit.abilities.ayatsuji_ayase

import com.tosware.nkm.models.game.abilities.ayatsuji_ayase.SamuraisSwiftness
import com.tosware.nkm.models.game.character.StatType
import com.tosware.nkm.models.game.hex.TestHexMapName
import helpers.{TestScenario, TestUtils}

class SamuraisSwiftnessSpec extends TestUtils {
  private val abilityMetadata = SamuraisSwiftness.metadata
  private val s = TestScenario.generate(TestHexMapName.Simple1v1, abilityMetadata.id)

  private val damagedGs = s.gameState.basicAttack(s.defaultCharacter.id, s.defaultEnemy.id)
  private val newPhaseGs = damagedGs.passAllCharactersInCurrentPhase()

  abilityMetadata.name must {
    "not give speed buff in the same turn after damaging someone" in {
      assertBuffDoesNotExist(StatType.Speed, s.defaultCharacter.id)(damagedGs)
    }

    "give speed buff turn after damaging someone" in {
      assertBuffExists(StatType.Speed, s.defaultCharacter.id)(newPhaseGs)
    }
  }
}
