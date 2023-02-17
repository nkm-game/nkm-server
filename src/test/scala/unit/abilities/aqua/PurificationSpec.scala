package unit.abilities.aqua

import com.tosware.nkm.NkmUtils
import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.abilities.aqua.Purification
import com.tosware.nkm.models.game.ability.UseData
import com.tosware.nkm.models.game.character.CharacterMetadata
import com.tosware.nkm.models.game.effects._
import helpers.{TestUtils, scenarios}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class PurificationSpec
  extends AnyWordSpecLike
    with Matchers
    with TestUtils
{
  private val metadata = CharacterMetadata.empty().copy(initialAbilitiesMetadataIds = Seq(Purification.metadata.id))
  private val s = scenarios.Simple2v2TestScenario(metadata)
  private val abilityId = s.characters.p0First.state.abilities.head.id
  private val effectGameState = s.gameState
    .addEffect(s.characters.p0Second.id, Disarm(NkmUtils.randomUUID(), 5))(random, s.gameState.id)
    .addEffect(s.characters.p0Second.id, Stun(NkmUtils.randomUUID(), 5))(random, s.gameState.id)
    .addEffect(s.characters.p0Second.id, Ground(NkmUtils.randomUUID(), 5))(random, s.gameState.id)
    .addEffect(s.characters.p0Second.id, Silence(NkmUtils.randomUUID(), 5))(random, s.gameState.id)

  Purification.metadata.name must {
    "be able to use" in {
      val r = GameStateValidator()(effectGameState)
        .validateAbilityUseOnCharacter(s.characters.p0First.owner(s.gameState).id, abilityId, s.characters.p0Second.id, UseData())
      assertCommandSuccess(r)
    }

    "be able to remove negative effects" in {
      val purifiedGameState: GameState = effectGameState.useAbilityOnCharacter(abilityId, s.characters.p0Second.id, UseData())
      purifiedGameState.characterById(s.characters.p0Second.id).state.effects should be (Seq.empty)
    }

    "not be able to use if target has no negative effects" in {
      val abilityId = s.characters.p0First.state.abilities.head.id
      val r = GameStateValidator()(s.gameState)
        .validateAbilityUseOnCharacter(s.characters.p0Second.owner(s.gameState).id ,abilityId, s.characters.p0Second.id, UseData())
      assertCommandFailure(r)
    }
  }
}