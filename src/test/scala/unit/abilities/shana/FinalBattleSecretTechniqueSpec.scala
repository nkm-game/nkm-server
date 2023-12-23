package unit.abilities.shana

import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.abilities.shana.FinalBattleSecretTechnique
import com.tosware.nkm.models.game.ability.UseData
import com.tosware.nkm.models.game.event.GameEvent
import com.tosware.nkm.models.game.hex.TestHexMapName
import helpers.{TestScenario, TestUtils}

class FinalBattleSecretTechniqueSpec extends TestUtils {
  private val abilityMetadata = FinalBattleSecretTechnique.metadata
  private val s = TestScenario.generate(TestHexMapName.FinalBattleSecretTechnique, abilityMetadata.id)
  private val gameState = s.ultGs
  private val abilityUsedGs = gameState.useAbility(s.defaultAbilityId, UseData(s.defaultEnemy.id))

  abilityMetadata.name must {
    "be able to use" in {
      assertCommandSuccess {
        GameStateValidator()(gameState)
          .validateAbilityUse(s.owners(0), s.defaultAbilityId, UseData(s.defaultEnemy.id))
      }
    }

    "knockback on use with shinku" in {
      s.defaultEnemy.parentCellOpt(abilityUsedGs).get
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
