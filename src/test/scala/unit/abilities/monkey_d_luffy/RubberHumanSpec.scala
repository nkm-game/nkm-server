package unit.abilities.monkey_d_luffy

import com.softwaremill.quicklens.ModifyPimp
import com.tosware.nkm.*
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.abilities.monkey_d_luffy.RubberHuman
import com.tosware.nkm.models.game.character.AttackType
import com.tosware.nkm.models.game.effects.Poison
import com.tosware.nkm.models.game.event.GameEvent.CharacterDamaged
import com.tosware.nkm.models.game.game_state.GameState
import com.tosware.nkm.models.game.hex.TestHexMapName
import helpers.{TestScenario, TestUtils}

class RubberHumanSpec extends TestUtils {
  private val abilityMetadata = RubberHuman.metadata
  private val s = TestScenario.generate(TestHexMapName.Simple1v1, abilityMetadata.id)
  private val rangedAttackerGs =
    s.gameState
      .updateCharacter(s.defaultEnemy.id)(_.modify(_.state.attackType).setTo(AttackType.Ranged))
  private val meleeAttackerGs =
    s.gameState
      .updateCharacter(s.defaultEnemy.id)(_.modify(_.state.attackType).setTo(AttackType.Melee))

  def getAttackGs(gs: GameState): GameState =
    gs
      .passTurn(s.defaultCharacter.id)
      .basicAttack(s.defaultEnemy.id, s.defaultCharacter.id)

  def getEffectGs(gs: GameState): GameState =
    gs
      .addEffect(
        s.defaultCharacter.id,
        Poison(randomUUID(), 2, Damage(DamageType.Physical, 30)),
      )(random, s.defaultEnemy.id)
      .passTurn(s.defaultCharacter.id)

  def getDamageDealt(gs: GameState): Int =
    gs
      .gameLog
      .events
      .ofType[CharacterDamaged]
      .head
      .damageAmount

  abilityMetadata.name must {
    "reduce ranged basic attack damage" in {
      getDamageDealt(getAttackGs(rangedAttackerGs)) should be < getDamageDealt(getAttackGs(meleeAttackerGs))
    }

    "not reduce ranged non basic attack damage" in {
      getDamageDealt(getEffectGs(rangedAttackerGs)) should be(getDamageDealt(getEffectGs(meleeAttackerGs)))
    }
  }
}
