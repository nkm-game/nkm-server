package unit.abilities.liones_elizabeth

import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.abilities.liones_elizabeth.Invigorate
import com.tosware.nkm.models.game.ability.UseData
import com.tosware.nkm.models.game.effects.HealOverTime
import com.tosware.nkm.models.game.hex.TestHexMapName
import helpers.{TestScenario, TestUtils}

class InvigorateSpec extends TestUtils {
  private val abilityMetadata = Invigorate.metadata
  private val s = TestScenario.generate(TestHexMapName.Simple1v1, abilityMetadata.id)
  private val damagedGs =
    s.gameState
      .damageCharacter(s.defaultCharacter.id, Damage(DamageType.True, 30))

  private val aGs: GameState = damagedGs.useAbility(s.defaultAbilityId, UseData(s.defaultCharacter.id))
  private val turnEndedGs: GameState = aGs.endTurn()
  private val secondTurnEndedGs: GameState =
    turnEndedGs
      .passTurn(s.defaultEnemy.id)
      .passTurn(s.defaultCharacter.id)

  def characterHp(gs: GameState): Int =
    gs.characterById(s.defaultCharacter.id).state.healthPoints

  abilityMetadata.name must {
    "be able to use" in {
      assertCommandSuccess {
        GameStateValidator()(damagedGs)
          .validateAbilityUse(s.owners(0), s.defaultAbilityId, UseData(s.defaultCharacter.id))
      }
    }

    "heal over time" in {
      assertEffectExistsOfType[HealOverTime](s.defaultCharacter.id)(aGs)
      characterHp(aGs) should be < characterHp(turnEndedGs)
      characterHp(turnEndedGs) should be < characterHp(secondTurnEndedGs)
    }
  }
}
