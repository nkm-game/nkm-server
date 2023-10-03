package unit.abilities.kirito

import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.abilities.kirito.StarburstStream
import com.tosware.nkm.models.game.character.CharacterMetadata
import helpers.{TestUtils, scenarios}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class StarburstStreamSpec
    extends AnyWordSpecLike
    with Matchers
    with TestUtils {
  private val initialHp = 10000
  private val abilityMetadata = StarburstStream.metadata
  private val characterMetadata = CharacterMetadata.empty()
    .copy(
      initialHealthPoints = initialHp,
      initialAbilitiesMetadataIds = Seq(abilityMetadata.id),
    )
  private val s = scenarios.Simple1v1TestScenario(characterMetadata)
  implicit private val gameState: GameState = s.gameState.incrementPhase(4)
  private val abilityId = s.p(0)(0).character.state.abilities.head.id

  abilityMetadata.name must {
    "be able to use" in {
      assertCommandSuccess {
        GameStateValidator()
          .validateAbilityUseOnCharacter(
            s.p(0)(0).character.owner.id,
            abilityId,
            s.p(1)(0).character.id,
          )
      }
    }

    "deal damage" in {
      val ngs = gameState.useAbilityOnCharacter(abilityId, s.p(1)(0).character.id)
      ngs.characterById(s.p(1)(0).character.id).state.healthPoints should be < initialHp
    }

    "allow attacking two times per turn" in {
      val ngs = gameState.useAbilityOnCharacter(abilityId, s.p(1)(0).character.id)
        .endTurn().passTurn(s.p(1)(0).character.id)
        .basicAttack(s.p(0)(0).character.id, s.p(1)(0).character.id)

      assertCommandSuccess {
        GameStateValidator()(ngs)
          .validateBasicAttackCharacter(
            s.p(0)(0).character.owner.id,
            s.p(0)(0).character.id,
            s.p(1)(0).character.id,
          )
      }

      val ngs2 = ngs.basicAttack(s.p(0)(0).character.id, s.p(1)(0).character.id)

      assertCommandFailure {
        GameStateValidator()(ngs2)
          .validateBasicAttackCharacter(
            s.p(0)(0).character.owner.id,
            s.p(0)(0).character.id,
            s.p(1)(0).character.id,
          )
      }

    }
  }
}
