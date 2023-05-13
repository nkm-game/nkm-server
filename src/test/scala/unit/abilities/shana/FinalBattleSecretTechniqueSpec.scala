package unit.abilities.shana

import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.abilities.shana.FinalBattleSecretTechnique
import com.tosware.nkm.models.game.event.GameEvent
import com.tosware.nkm.models.game.hex.TestHexMapName
import helpers.{TestScenario, TestUtils}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class FinalBattleSecretTechniqueSpec
  extends AnyWordSpecLike
    with Matchers
    with TestUtils
{
  private val abilityMetadata = FinalBattleSecretTechnique.metadata
  private val s = TestScenario.generate(TestHexMapName.FinalBattleSecretTechnique, abilityMetadata.id)
  private val gameState = s.gameState.incrementPhase(4)
  private val abilityUsedGs = gameState.useAbilityOnCharacter(s.defaultAbilityId, s.p(1)(0).character.id)

  abilityMetadata.name must {
    "be able to use" in {
      assertCommandSuccess {
        GameStateValidator()(gameState)
          .validateAbilityUseOnCharacter(s.owners(0), s.defaultAbilityId, s.p(1)(0).character.id)
      }
    }

    "knockback on use with shinku" in {
      s.p(1)(0).character.parentCell(abilityUsedGs).get
        .coordinates.toTuple shouldBe (7, 0)
    }

    "damage 3 characters with hien" in {
      abilityUsedGs.gameLog.events
        .ofType[GameEvent.DamageSent]
        .causedBy(s.defaultAbilityId)
        .count(_.damage.damageType == DamageType.Magical) shouldBe 3
    }

    "damage one character with shinpan and danzai" in {
      abilityUsedGs.gameLog.events
        .ofType[GameEvent.DamageSent]
        .causedBy(s.defaultAbilityId)
        .count(_.damage.damageType == DamageType.True) shouldBe 1
    }

    "deal 10 damage with shinpan and danzai" in {
      abilityUsedGs.gameLog.events
        .ofType[GameEvent.DamageSent]
        .causedBy(s.defaultAbilityId)
        .filter(_.damage.damageType == DamageType.True)
        .map(_.damage.amount)
        .toSet should contain only 10
    }
  }
}