package unit.abilities.kirito

import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.abilities.kirito.StarburstStream
import com.tosware.nkm.models.game.character.CharacterMetadata
import helpers.{TestUtils, scenarios}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class StarburstStreamSpec
  extends AnyWordSpecLike
    with Matchers
    with TestUtils
{
  private val initialHp = 10000
  private val abilityMetadata = StarburstStream.metadata
  private val characterMetadata = CharacterMetadata.empty()
    .copy(
      initialHealthPoints = initialHp,
      initialAbilitiesMetadataIds = Seq(abilityMetadata.id))
  private val s = scenarios.Simple1v1TestScenario(characterMetadata)
  private implicit val gameState: GameState = s.gameState.incrementPhase(4)
  private val abilityId = s.characters.p0.state.abilities.head.id

  abilityMetadata.name must {
    "be able to use" in {
      assertCommandSuccess {
        GameStateValidator()
          .validateAbilityUseOnCharacter(
            s.characters.p0.owner.id,
            abilityId,
            s.characters.p1.id,
          )
      }
    }

    "deal damage" in {
      val ngs = gameState.useAbilityOnCharacter(abilityId, s.characters.p1.id)
      ngs.characterById(s.characters.p1.id).state.healthPoints should be < initialHp
    }

    "allow attacking two times per turn" in {
      val ngs = gameState.useAbilityOnCharacter(abilityId, s.characters.p1.id)
        .endTurn().passTurn(s.characters.p1.id)
        .basicAttack(s.characters.p0.id, s.characters.p1.id)

      assertCommandSuccess {
        GameStateValidator()(ngs)
          .validateBasicAttackCharacter(
            s.characters.p0.owner.id,
            s.characters.p0.id,
            s.characters.p1.id,
          )
      }

      val ngs2 = ngs.basicAttack(s.characters.p0.id, s.characters.p1.id)

      assertCommandFailure {
        GameStateValidator()(ngs2)
          .validateBasicAttackCharacter(
            s.characters.p0.owner.id,
            s.characters.p0.id,
            s.characters.p1.id,
          )
      }

    }
  }
}