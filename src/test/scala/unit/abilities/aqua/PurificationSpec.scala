package unit.abilities.aqua

import com.tosware.nkm.*
import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.abilities.aqua.Purification
import com.tosware.nkm.models.game.ability.UseData
import com.tosware.nkm.models.game.character.CharacterMetadata
import com.tosware.nkm.models.game.effects.*
import helpers.{TestUtils, scenarios}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class PurificationSpec
  extends AnyWordSpecLike
    with Matchers
    with TestUtils
{
  private val abilityMetadata = Purification.metadata
  private val metadata = CharacterMetadata.empty().copy(initialAbilitiesMetadataIds = Seq(abilityMetadata.id))
  private val s = scenarios.Simple2v2TestScenario(metadata)
  private val abilityId = s.p(0)(0).character.state.abilities.head.id
  private val effectGameState = s.gameState
    .addEffect(s.p(0)(1).character.id, Disarm(randomUUID(), 5))(random, s.gameState.id)
    .addEffect(s.p(0)(1).character.id, Stun(randomUUID(), 5))(random, s.gameState.id)
    .addEffect(s.p(0)(1).character.id, Ground(randomUUID(), 5))(random, s.gameState.id)
    .addEffect(s.p(0)(1).character.id, Silence(randomUUID(), 5))(random, s.gameState.id)

  abilityMetadata.name must {
    "be able to use" in {
      assertCommandSuccess {
        GameStateValidator()(effectGameState)
          .validateAbilityUseOnCharacter(s.p(0)(0).ownerId, abilityId, s.p(0)(1).character.id, UseData())
      }
    }

    "be able to remove negative effects" in {
      val purifiedGameState: GameState = effectGameState.useAbilityOnCharacter(abilityId, s.p(0)(1).character.id, UseData())
      purifiedGameState.characterById(s.p(0)(1).character.id).state.effects should be (Seq.empty)
    }

    "not be able to use if target has no negative effects" in {
      val abilityId = s.p(0)(0).character.state.abilities.head.id
      assertCommandFailure {
        GameStateValidator()(s.gameState)
          .validateAbilityUseOnCharacter(s.p(0)(1).ownerId ,abilityId, s.p(0)(1).character.id, UseData())
      }
    }
  }
}