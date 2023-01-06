package unit.abilities.kirito

import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.abilities.hecate.PowerOfExistence
import com.tosware.nkm.models.game.abilities.kirito.Switch
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
  private val abilityId = s.characters.p0First.state.abilities.head.id
  private val ultimateAbilityId = s.characters.p0First.state.abilities.tail.head.id

  abilityMetadata.name must {
    "be able to switch" when {
      "character is in range of an enemy" in {
        val ngs = gameState.teleportCharacter(s.characters.p0First.id, HexCoordinates(2, 0))
        assertCommandSuccess {
          GameStateValidator()(ngs)
            .validateAbilityUseOnCharacter(
              s.characters.p0First.owner.id,
              abilityId,
              s.characters.p0Second.id,
            )
        }
      }
      "friend is in range of an enemy" in {
        val ngs = gameState.teleportCharacter(s.characters.p0Second.id, HexCoordinates(2, 0))
        assertCommandSuccess {
          GameStateValidator()(ngs)
            .validateAbilityUseOnCharacter(
              s.characters.p0First.owner.id,
              abilityId,
              s.characters.p0Second.id,
            )
        }
      }
    }

    "be able to basic attack after using switch" in {
      val ngs = gameState
        .teleportCharacter(s.characters.p0Second.id, HexCoordinates(2, 0))
        .useAbilityOnCharacter(abilityId, s.characters.p0Second.id)

      assertCommandSuccess {
        GameStateValidator()(ngs)
          .validateBasicAttackCharacter(
            s.characters.p0First.owner.id,
            s.characters.p0First.id,
            s.characters.p1First.id,
          )
      }
    }

    "be able to use ultimate ability after using switch" in {
      val ngs = gameState
        .teleportCharacter(s.characters.p0Second.id, HexCoordinates(2, 0))
        .useAbilityOnCharacter(abilityId, s.characters.p0Second.id)

      assertCommandSuccess {
        GameStateValidator()(ngs)
          .validateAbilityUseWithoutTarget(
            s.characters.p0First.owner.id,
            ultimateAbilityId,
          )
      }
    }

    "not be able switch when character and friend are not in range of an enemy" in {
      assertCommandFailure {
        GameStateValidator()(gameState)
          .validateAbilityUseOnCharacter(
            s.characters.p0First.owner.id,
            abilityId,
            s.characters.p0Second.id,
          )
      }
    }

    "not be able to use switch on enemies" in {
      assertCommandFailure {
        GameStateValidator()(gameState)
          .validateAbilityUseOnCharacter(
            s.characters.p0First.owner.id,
            abilityId,
            s.characters.p1First.id,
          )
      }
    }

    "not be able to use switch on character outside map" in {
        val ngs = gameState
          .teleportCharacter(s.characters.p0First.id, HexCoordinates(2, 0))
          .removeCharacterFromMap(s.characters.p0Second.id)

        assertCommandFailure {
          GameStateValidator()(ngs)
            .validateAbilityUseOnCharacter(
              s.characters.p0First.owner.id,
              abilityId,
              s.characters.p0Second.id,
            )
        }
    }
  }
}