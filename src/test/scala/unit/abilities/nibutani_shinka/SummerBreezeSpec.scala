package unit.abilities.nibutani_shinka

import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.abilities.nibutani_shinka.SummerBreeze
import com.tosware.nkm.models.game.character.CharacterMetadata
import helpers.{TestUtils, scenarios}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class SummerBreezeSpec
  extends AnyWordSpecLike
    with Matchers
    with TestUtils
{

  private val abilityMetadata = SummerBreeze.metadata
  private val metadata = CharacterMetadata.empty().copy(initialAbilitiesMetadataIds = Seq(abilityMetadata.id))
  private val s = scenarios.SummerBreezeTestScenario(metadata)
  private val gameState: GameState = s.gameState
  private val abilityId = s.characters.p0.state.abilities.head.id

  private val abilityUsedOnP1FirstGs = gameState.useAbilityOnCharacter(abilityId, s.characters.p1First.id)
  private val abilityUsedOnP1SecondGs = gameState.useAbilityOnCharacter(abilityId, s.characters.p1Second.id)
  private val abilityUsedOnP1FirstWithoutSecondGs = gameState
    .executeCharacter(s.characters.p1Second.id)
    .useAbilityOnCharacter(abilityId, s.characters.p1First.id)

  abilityMetadata.name must {
    "be able to use" in {
      assertCommandSuccess {
        GameStateValidator()(gameState)
          .validateAbilityUseOnCharacter(s.characters.p0.owner(gameState).id, abilityId, s.characters.p1First.id)
      }

      assertCommandSuccess {
        GameStateValidator()(gameState)
          .validateAbilityUseOnCharacter(s.characters.p0.owner(gameState).id, abilityId, s.characters.p1Second.id)
      }
    }

    "knockback on use till wall or character" in {
      s.characters.p1First.parentCell(abilityUsedOnP1FirstGs).get
        .coordinates.toTuple shouldBe (3, 0)
      s.characters.p1Second.parentCell(abilityUsedOnP1SecondGs).get
        .coordinates.toTuple shouldBe (7, 0)
      s.characters.p1First.parentCell(abilityUsedOnP1FirstWithoutSecondGs).get
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