package unit.abilities.aqua

import com.tosware.nkm.*
import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.abilities.aqua.Purification
import com.tosware.nkm.models.game.ability.UseData
import com.tosware.nkm.models.game.character.CharacterMetadata
import com.tosware.nkm.models.game.effects.*
import com.tosware.nkm.models.game.hex.TestHexMapName
import helpers.{TestScenario, TestUtils}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class PurificationSpec
    extends AnyWordSpecLike
    with Matchers
    with TestUtils {
  private val abilityMetadata = Purification.metadata
  private val metadata = CharacterMetadata.empty().copy(initialAbilitiesMetadataIds = Seq(abilityMetadata.id))
  private val s = TestScenario.generate(TestHexMapName.Simple2v2, metadata)
  private val abilityId = s.defaultAbilityId
  private val effectGameState = s.gameState
    .addEffect(s.p(0)(1).character.id, Disarm(randomUUID(), 5))(random, s.gameState.id)
    .addEffect(s.p(0)(1).character.id, Stun(randomUUID(), 5))(random, s.gameState.id)
    .addEffect(s.p(0)(1).character.id, Ground(randomUUID(), 5))(random, s.gameState.id)
    .addEffect(s.p(0)(1).character.id, Silence(randomUUID(), 5))(random, s.gameState.id)

  abilityMetadata.name must {
    "be able to use" in {
      assertCommandSuccess {
        GameStateValidator()(effectGameState)
          .validateAbilityUse(s.owners(0), abilityId, UseData(s.p(0)(1).character.id))
      }
    }

    "be able to remove negative effects" in {
      val purifiedGameState: GameState =
        effectGameState.useAbility(abilityId, UseData(s.p(0)(1).character.id))
      purifiedGameState.characterById(s.p(0)(1).character.id).state.effects should be(Seq.empty)
    }

    "not be able to use if target has no negative effects" in {
      val abilityId = s.defaultAbilityId
      assertCommandFailure {
        GameStateValidator()(s.gameState)
          .validateAbilityUse(s.owners(0), abilityId, UseData(s.p(0)(1).character.id))
      }
    }
  }
}
