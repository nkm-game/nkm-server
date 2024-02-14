package unit.abilities.shana

import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.abilities.shana.WingsOfCrimson
import com.tosware.nkm.models.game.character.{CharacterMetadata, StatType}
import com.tosware.nkm.models.game.game_state.GameState
import com.tosware.nkm.models.game.hex.TestHexMapName
import helpers.{TestScenario, TestUtils}

class WingsOfCrimsonSpec extends TestUtils {
  private val abilityMetadata = WingsOfCrimson.metadata
  private val characterMetadata = CharacterMetadata.empty().copy(initialAbilitiesMetadataIds = Seq(abilityMetadata.id))
  private val s = TestScenario.generate(TestHexMapName.Simple1v1, characterMetadata)
  private val gameState: GameState = s.gameState.passTurn(s.defaultCharacter.id)

  abilityMetadata.name must {
    "apply flying and speed buff effects on being basic attacked" in {
      val ngs: GameState = gameState.basicAttack(s.defaultEnemy.id, s.defaultCharacter.id)

      assertEffectExistsOfType[effects.Fly](s.defaultCharacter.id)(ngs)
      assertBuffExists(StatType.Speed, s.defaultCharacter.id)(ngs)
    }

    "apply flying and speed buff effects on being damaged" in {
      val ngs: GameState = gameState.damageCharacter(s.defaultCharacter.id, Damage(DamageType.True, 1))

      assertEffectExistsOfType[effects.Fly](s.defaultCharacter.id)(ngs)
      assertBuffExists(StatType.Speed, s.defaultCharacter.id)(ngs)
    }

    "refresh flying and speed buff effects on being damaged when the effects are active" in {
      val ngs: GameState = gameState
        .damageCharacter(s.defaultCharacter.id, Damage(DamageType.True, 1))
        .passTurn(s.defaultCharacter.id)
        .damageCharacter(s.defaultCharacter.id, Damage(DamageType.True, 1))

      assertEffectSingleOfType[effects.Fly](s.defaultCharacter.id)(ngs)
      assertEffectSingleOfType[effects.StatBuff](s.defaultCharacter.id)(ngs)

      firstEffectOfType[effects.Fly](s.defaultCharacter.id)(ngs)
        .state(ngs)
        .cooldown should be(abilityMetadata.variables("duration"))

      firstEffectOfType[effects.StatBuff](s.defaultCharacter.id)(ngs)
        .state(ngs)
        .cooldown should be(abilityMetadata.variables("duration"))
    }
  }
}
