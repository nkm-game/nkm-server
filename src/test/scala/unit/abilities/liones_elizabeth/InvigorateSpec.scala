package unit.abilities.liones_elizabeth

import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.abilities.liones_elizabeth.Invigorate
import com.tosware.nkm.models.game.character.CharacterMetadata
import helpers.{TestUtils, scenarios}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class InvigorateSpec
  extends AnyWordSpecLike
    with Matchers
    with TestUtils
{
  private val abilityMetadata = Invigorate.metadata
  private val metadata = CharacterMetadata.empty().copy(initialAbilitiesMetadataIds = Seq(abilityMetadata.id))
  private val s = scenarios.Simple1v1TestScenario(metadata)
  private val abilityId = s.p(0)(0).character.state.abilities.head.id
  private val damagedGameState = s.gameState
    .damageCharacter(s.p(0)(0).character.id, Damage(DamageType.True, 30))(random, s.gameState.id)

  abilityMetadata.name must {
    "be able to use" in {
      assertCommandSuccess {
        GameStateValidator()(damagedGameState)
          .validateAbilityUseOnCharacter(s.p(0)(0).ownerId, abilityId, s.p(0)(0).character.id)
      }
    }

    "heal over time" in {
      val ags: GameState = damagedGameState.useAbilityOnCharacter(abilityId, s.p(0)(0).character.id)

      val ngs1 = ags
        .endTurn()
        .passTurn(s.p(1)(0).character.id)

      val ngs2 = ngs1
        .passTurn(s.p(0)(0).character.id)

      val initialHp = ags.characterById(s.p(0)(0).character.id).state.healthPoints
      val hp1 = ngs1.characterById(s.p(0)(0).character.id).state.healthPoints
      val hp2 = ngs2.characterById(s.p(0)(0).character.id).state.healthPoints

      initialHp should be < hp1
      hp1 should be < hp2
    }
  }
}