package unit.abilities.kirito

import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.abilities.kirito.StarburstStream
import com.tosware.nkm.models.game.ability.UseData
import com.tosware.nkm.models.game.character.CharacterMetadata
import com.tosware.nkm.models.game.hex.TestHexMapName
import helpers.{TestScenario, TestUtils}
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
  private val s = TestScenario.generate(TestHexMapName.Simple1v1, characterMetadata)
  implicit private val gameState: GameState = s.ultGs
  private val abilityId = s.defaultAbilityId

  abilityMetadata.name must {
    "be able to use" in {
      assertCommandSuccess {
        GameStateValidator()
          .validateAbilityUse(
            s.owners(0),
            abilityId,
            UseData(s.defaultEnemy.id),
          )
      }
    }

    "deal damage" in {
      val ngs = gameState.useAbility(abilityId, UseData(s.defaultEnemy.id))
      ngs.characterById(s.defaultEnemy.id).state.healthPoints should be < initialHp
    }

    "allow attacking two times per turn" in {
      val ngs = gameState.useAbility(abilityId, UseData(s.defaultEnemy.id))
        .endTurn().passTurn(s.defaultEnemy.id)
        .basicAttack(s.defaultCharacter.id, s.defaultEnemy.id)

      assertCommandSuccess {
        GameStateValidator()(ngs)
          .validateBasicAttackCharacter(
            s.owners(0),
            s.defaultCharacter.id,
            s.defaultEnemy.id,
          )
      }

      val ngs2 = ngs.basicAttack(s.defaultCharacter.id, s.defaultEnemy.id)

      assertCommandFailure {
        GameStateValidator()(ngs2)
          .validateBasicAttackCharacter(
            s.owners(0),
            s.defaultCharacter.id,
            s.defaultEnemy.id,
          )
      }

    }
  }
}
