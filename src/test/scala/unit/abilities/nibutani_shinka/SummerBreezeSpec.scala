package unit.abilities.nibutani_shinka

import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.abilities.nibutani_shinka.SummerBreeze
import com.tosware.nkm.models.game.ability.UseData
import com.tosware.nkm.models.game.event.GameEvent
import com.tosware.nkm.models.game.hex.TestHexMapName
import helpers.{TestScenario, TestUtils}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class SummerBreezeSpec
    extends AnyWordSpecLike
    with Matchers
    with TestUtils {
  private val abilityMetadata = SummerBreeze.metadata
  private val s = TestScenario.generate(TestHexMapName.SummerBreeze, abilityMetadata.id)
  private val gameState: GameState = s.gameState
  private val abilityId = s.defaultAbilityId

  private val abilityUsedOnP1FirstGs = gameState.useAbility(abilityId, UseData(s.defaultEnemy.id))
  private val abilityUsedOnP1SecondGs = gameState.useAbility(abilityId, UseData(s.p(1)(1).character.id))
  private val abilityUsedOnP1FirstWithoutSecondGs = gameState
    .executeCharacter(s.p(1)(1).character.id)
    .useAbility(abilityId, UseData(s.defaultEnemy.id))

  abilityMetadata.name must {
    "be able to use" in {
      assertCommandSuccess {
        GameStateValidator()(gameState)
          .validateAbilityUse(s.owners(0), abilityId, UseData(s.defaultEnemy.id))
      }

      assertCommandSuccess {
        GameStateValidator()(gameState)
          .validateAbilityUse(s.owners(0), abilityId, UseData(s.p(1)(1).character.id))
      }
    }

    "knockback on use till wall or character" in {
      s.defaultEnemy.parentCellOpt(abilityUsedOnP1FirstGs).get
        .coordinates.toTuple shouldBe (3, 0)
      s.p(1)(1).character.parentCellOpt(abilityUsedOnP1SecondGs).get
        .coordinates.toTuple shouldBe (7, 0)
      s.defaultEnemy.parentCellOpt(abilityUsedOnP1FirstWithoutSecondGs).get
        .coordinates.toTuple shouldBe (7, 0)
    }

    "damage if knocked into wall or character" in {
      abilityUsedOnP1FirstGs.gameLog.events
        .ofType[GameEvent.CharacterDamaged]
        .causedBy(abilityId)
        .size shouldBe 1

      abilityUsedOnP1SecondGs.gameLog.events
        .ofType[GameEvent.CharacterDamaged]
        .causedBy(abilityId)
        .size shouldBe 1

      abilityUsedOnP1FirstWithoutSecondGs.gameLog.events
        .ofType[GameEvent.CharacterDamaged]
        .causedBy(abilityId)
        .size shouldBe 0
    }

    "stun if knocked into wall or character" in {
      abilityUsedOnP1FirstGs.gameLog.events
        .ofType[GameEvent.EffectAddedToCharacter]
        .causedBy(abilityId)
        .size shouldBe 1

      abilityUsedOnP1SecondGs.gameLog.events
        .ofType[GameEvent.EffectAddedToCharacter]
        .causedBy(abilityId)
        .size shouldBe 1

      abilityUsedOnP1FirstWithoutSecondGs.gameLog.events
        .ofType[GameEvent.EffectAddedToCharacter]
        .causedBy(abilityId)
        .size shouldBe 0
    }
  }
}
