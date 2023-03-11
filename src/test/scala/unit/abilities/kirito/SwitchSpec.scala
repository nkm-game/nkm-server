package unit.abilities.kirito

import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.abilities.hecate.PowerOfExistence
import com.tosware.nkm.models.game.abilities.kirito.Switch
import com.tosware.nkm.models.game.character.CharacterMetadata
import com.tosware.nkm.models.game.hex.HexCoordinates
import helpers.{TestUtils, scenarios}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class SwitchSpec
  extends AnyWordSpecLike
    with Matchers
    with TestUtils
{
  private val abilityMetadata = Switch.metadata
  private val ultimateAbilityMetadata = PowerOfExistence.metadata
  private val characterMetadata = CharacterMetadata.empty()
    .copy(
      initialBasicAttackRange = 1,
      initialAbilitiesMetadataIds = Seq(abilityMetadata.id, ultimateAbilityMetadata.id)
    )
  private val s = scenarios.Simple2v2TestScenario(characterMetadata)
  private implicit val gameState: GameState = s.gameState.incrementPhase(4)
  private val abilityId = s.p(0)(0).character.state.abilities.head.id
  private val ultimateAbilityId = s.p(0)(0).character.state.abilities.tail.head.id

  abilityMetadata.name must {
    "be able to switch" when {
      "character is in range of an enemy" in {
        val ngs = gameState.teleportCharacter(s.p(0)(0).character.id, HexCoordinates(2, 0))
        assertCommandSuccess {
          GameStateValidator()(ngs)
            .validateAbilityUseOnCharacter(
              s.p(0)(0).character.owner.id,
              abilityId,
              s.p(0)(1).character.id,
            )
        }
      }
      "friend is in range of an enemy" in {
        val ngs = gameState.teleportCharacter(s.p(0)(1).character.id, HexCoordinates(2, 0))
        assertCommandSuccess {
          GameStateValidator()(ngs)
            .validateAbilityUseOnCharacter(
              s.p(0)(0).character.owner.id,
              abilityId,
              s.p(0)(1).character.id,
            )
        }
      }
    }

    "be able to basic attack after using switch" in {
      val ngs = gameState
        .teleportCharacter(s.p(0)(1).character.id, HexCoordinates(2, 0))
        .useAbilityOnCharacter(abilityId, s.p(0)(1).character.id)

      assertCommandSuccess {
        GameStateValidator()(ngs)
          .validateBasicAttackCharacter(
            s.p(0)(0).character.owner.id,
            s.p(0)(0).character.id,
            s.p(1)(0).character.id,
          )
      }
    }

    "be able to use ultimate ability after using switch" in {
      val ngs = gameState
        .teleportCharacter(s.p(0)(1).character.id, HexCoordinates(2, 0))
        .useAbilityOnCharacter(abilityId, s.p(0)(1).character.id)

      assertCommandSuccess {
        GameStateValidator()(ngs)
          .validateAbilityUse(
            s.p(0)(0).character.owner.id,
            ultimateAbilityId,
          )
      }
    }

    "not be able switch when character and friend are not in range of an enemy" in {
      assertCommandFailure {
        GameStateValidator()(gameState)
          .validateAbilityUseOnCharacter(
            s.p(0)(0).character.owner.id,
            abilityId,
            s.p(0)(1).character.id,
          )
      }
    }

    "not be able to use switch on enemies" in {
      assertCommandFailure {
        GameStateValidator()(gameState)
          .validateAbilityUseOnCharacter(
            s.p(0)(0).character.owner.id,
            abilityId,
            s.p(1)(0).character.id,
          )
      }
    }

    "not be able to use switch on character outside map" in {
        val ngs = gameState
          .teleportCharacter(s.p(0)(0).character.id, HexCoordinates(2, 0))
          .removeCharacterFromMap(s.p(0)(1).character.id)

        assertCommandFailure {
          GameStateValidator()(ngs)
            .validateAbilityUseOnCharacter(
              s.p(0)(0).character.owner.id,
              abilityId,
              s.p(0)(1).character.id,
            )
        }
    }
  }
}