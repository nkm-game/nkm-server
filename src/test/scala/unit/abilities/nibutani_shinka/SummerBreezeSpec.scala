package unit.abilities.nibutani_shinka

import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.abilities.nibutani_shinka.SummerBreeze
import com.tosware.nkm.models.game.character.CharacterMetadata
import com.tosware.nkm.models.game.event.GameEvent
import helpers.{TestUtils, scenarios}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class SummerBreezeSpec
    extends AnyWordSpecLike
    with Matchers
    with TestUtils {
  private val abilityMetadata = SummerBreeze.metadata
  private val characterMetadata = CharacterMetadata.empty().copy(initialAbilitiesMetadataIds = Seq(abilityMetadata.id))
  private val s = scenarios.SummerBreezeTestScenario(characterMetadata)
  private val gameState: GameState = s.gameState
  private val abilityId = s.p(0)(0).character.state.abilities.head.id

  private val abilityUsedOnP1FirstGs = gameState.useAbilityOnCharacter(abilityId, s.p(1)(0).character.id)
  private val abilityUsedOnP1SecondGs = gameState.useAbilityOnCharacter(abilityId, s.p(1)(1).character.id)
  private val abilityUsedOnP1FirstWithoutSecondGs = gameState
    .executeCharacter(s.p(1)(1).character.id)
    .useAbilityOnCharacter(abilityId, s.p(1)(0).character.id)

  abilityMetadata.name must {
    "be able to use" in {
      assertCommandSuccess {
        GameStateValidator()(gameState)
          .validateAbilityUseOnCharacter(s.p(0)(0).ownerId, abilityId, s.p(1)(0).character.id)
      }

      assertCommandSuccess {
        GameStateValidator()(gameState)
          .validateAbilityUseOnCharacter(s.p(0)(0).ownerId, abilityId, s.p(1)(1).character.id)
      }
    }

    "knockback on use till wall or character" in {
      s.p(1)(0).character.parentCell(abilityUsedOnP1FirstGs).get
        .coordinates.toTuple shouldBe (3, 0)
      s.p(1)(1).character.parentCell(abilityUsedOnP1SecondGs).get
        .coordinates.toTuple shouldBe (7, 0)
      s.p(1)(0).character.parentCell(abilityUsedOnP1FirstWithoutSecondGs).get
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
