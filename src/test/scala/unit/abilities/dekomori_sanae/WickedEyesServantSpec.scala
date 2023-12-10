package unit.abilities.dekomori_sanae

import com.softwaremill.quicklens.*
import com.tosware.nkm.*
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.abilities.dekomori_sanae.WickedEyesServant
import com.tosware.nkm.models.game.event.GameEvent.CharacterDamaged
import com.tosware.nkm.models.game.hex.TestHexMapName
import helpers.{TestScenario, TestUtils}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class WickedEyesServantSpec
    extends AnyWordSpecLike
    with Matchers
    with TestUtils {

  private val abilityMetadata = WickedEyesServant.metadata
  private val s = TestScenario.generate(TestHexMapName.Simple2v2, abilityMetadata.id)

  private val moreAdGs: GameState =
    s.gameState
      .updateCharacter(s.defaultEnemy.id)(_.modify(_.state.pureAttackPoints).setTo(1000))

  private val takanashiGs: GameState =
    s.gameState
      .updateCharacter(s.defaultEnemy.id)(_.modify(_.state.name).setTo("Rikka Takanashi"))

  private def killGs(gs: GameState) =
    gs.executeCharacter(s.p(1)(1).character.id)(random, s.defaultCharacter.id)

  private def damagedGs(gs: GameState) =
    gs.basicAttack(s.defaultCharacter.id, s.defaultEnemy.id)

  private def damageAmountsCausedByAbility(gs: GameState) =
    gs
      .gameLog
      .events
      .ofType[CharacterDamaged]
      .causedBy(s.defaultAbilityId)
      .map(_.damageAmount)

  abilityMetadata.name must {
    "not be active without a reason" in {
      damageAmountsCausedByAbility(damagedGs(s.gameState))
        .size should be(0)
    }
    "be active when someone has more AD on the map" in {
      damageAmountsCausedByAbility(damagedGs(moreAdGs))
        .size should be(1)
    }
    "be active when Rikka Takanashi is on the map" in {
      damageAmountsCausedByAbility(damagedGs(takanashiGs))
        .size should be(1)
    }
    "add one dmg on kill" in {
      val defaultDamage = damageAmountsCausedByAbility(damagedGs(moreAdGs)).head
      val damageAfterKill = damageAmountsCausedByAbility(damagedGs(killGs(moreAdGs))).head
      damageAfterKill should be(defaultDamage + 1)
    }
  }
}
