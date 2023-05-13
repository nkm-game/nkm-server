package unit.abilities.liones_elizabeth

import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.abilities.liones_elizabeth.Invigorate
import com.tosware.nkm.models.game.effects.HealOverTime
import com.tosware.nkm.models.game.hex.TestHexMapName
import helpers.{TestScenario, TestUtils}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class InvigorateSpec
  extends AnyWordSpecLike
    with Matchers
    with TestUtils
{
  private val abilityMetadata = Invigorate.metadata
  private val s = TestScenario.generate(TestHexMapName.Simple1v1, abilityMetadata.id)
  private val damagedGs =
    s.gameState
      .damageCharacter(s.p(0)(0).character.id, Damage(DamageType.True, 30))

  private val aGs: GameState = damagedGs.useAbilityOnCharacter(s.defaultAbilityId, s.p(0)(0).character.id)
  private val turnEndedGs: GameState = aGs.endTurn()
  private val secondTurnEndedGs: GameState =
    turnEndedGs
      .passTurn(s.p(1)(0).character.id)
      .passTurn(s.p(0)(0).character.id)

  def characterHp(gs: GameState): Int =
    gs.characterById(s.defaultCharacter.id).state.healthPoints

  abilityMetadata.name must {
    "be able to use" in {
      assertCommandSuccess {
        GameStateValidator()(damagedGs)
          .validateAbilityUseOnCharacter(s.owners(0), s.defaultAbilityId, s.defaultCharacter.id)
      }
    }

    "heal over time" in {
      assertEffectExistsOfType[HealOverTime](s.defaultCharacter.id)(aGs)
      characterHp(aGs) should be < characterHp(turnEndedGs)
      characterHp(turnEndedGs) should be < characterHp(secondTurnEndedGs)
    }
  }
}