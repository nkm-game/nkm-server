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

  def getDamageDealtByAttack(gs: GameState): Int =
    gs
      .passTurn(s.defaultCharacter.id)
      .basicAttack(s.defaultEnemy.id, s.defaultCharacter.id)
      .gameLog
      .events
      .ofType[CharacterDamaged]
      .head
      .damageAmount

  def getDamageDealtByEffect(gs: GameState): Int =
    gs
      .addEffect(
        s.defaultCharacter.id,
        Poison(randomUUID(), 2, Damage(DamageType.Physical, 30)),
      )(random, s.defaultEnemy.id)
      .passTurn(s.defaultCharacter.id)
      .gameLog
      .events
      .ofType[CharacterDamaged]
      .head
      .damageAmount

  abilityMetadata.name must {
    "reduce ranged basic attack damage" in {
      getDamageDealtByAttack(rangedAttackerGs) should be < getDamageDealtByAttack(meleeAttackerGs)
    }

    "not reduce ranged non basic attack damage" in {
      getDamageDealtByEffect(rangedAttackerGs) should be(getDamageDealtByEffect(meleeAttackerGs))
    }
  }
}
