package unit.abilities.kirito

import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game.abilities.hecate.PowerOfExistence
import com.tosware.nkm.models.game.abilities.kirito.Switch
import com.tosware.nkm.models.game.ability.UseData
import com.tosware.nkm.models.game.character.CharacterMetadata
import com.tosware.nkm.models.game.game_state.GameState
import com.tosware.nkm.models.game.hex.{HexCoordinates, TestHexMapName}
import helpers.{TestScenario, TestUtils}

class SwitchSpec extends TestUtils {
  private val abilityMetadata = Switch.metadata
  private val ultimateAbilityMetadata = PowerOfExistence.metadata
  private val characterMetadata = CharacterMetadata.empty()
    .copy(
      initialBasicAttackRange = 1,
      initialAbilitiesMetadataIds = Seq(abilityMetadata.id, ultimateAbilityMetadata.id),
    )
  private val s = TestScenario.generate(TestHexMapName.Simple2v2, characterMetadata)
  implicit private val gameState: GameState = s.ultGs
  private val abilityId = s.defaultAbilityId
  private val ultimateAbilityId = s.defaultCharacter.state.abilities.tail.head.id

  abilityMetadata.name must {
    "be able to switch" when {
      "character is in range of an enemy" in {
        val ngs = gameState.teleportCharacter(s.defaultCharacter.id, HexCoordinates(2, 0))
        assertCommandSuccess {
          GameStateValidator()(ngs)
            .validateAbilityUse(s.owners(0), abilityId, UseData(s.p(0)(1).character.id))
        }
      }
      "friend is in range of an enemy" in {
        val ngs = gameState.teleportCharacter(s.p(0)(1).character.id, HexCoordinates(2, 0))
        assertCommandSuccess {
          GameStateValidator()(ngs)
            .validateAbilityUse(s.owners(0), abilityId, UseData(s.p(0)(1).character.id))
        }
      }
    }

    "be able to basic attack after using switch" in {
      val ngs = gameState
        .teleportCharacter(s.p(0)(1).character.id, HexCoordinates(2, 0))
        .useAbility(abilityId, UseData(s.p(0)(1).character.id))

      assertCommandSuccess {
        GameStateValidator()(ngs)
          .validateBasicAttackCharacter(
            s.owners(0),
            s.defaultCharacter.id,
            s.defaultEnemy.id,
          )
      }
    }

    "be able to use ultimate ability after using switch" in {
      val ngs = gameState
        .teleportCharacter(s.p(0)(1).character.id, HexCoordinates(2, 0))
        .useAbility(abilityId, UseData(s.p(0)(1).character.id))

      assertCommandSuccess {
        GameStateValidator()(ngs)
          .validateAbilityUse(
            s.owners(0),
            ultimateAbilityId,
          )
      }
    }

    "not be able to attack and use ultimate ability after using switch" in {
      val ngs = gameState
        .teleportCharacter(s.p(0)(1).character.id, HexCoordinates(2, 0))
        .useAbility(abilityId, UseData(s.p(0)(1).character.id))
        .basicAttack(s.defaultCharacter.id, s.defaultEnemy.id)

      assertCommandFailure {
        GameStateValidator()(ngs)
          .validateAbilityUse(
            s.owners(0),
            ultimateAbilityId,
          )
      }
    }

    "not be able switch when character and friend are not in range of an enemy" in {
      assertCommandFailure {
        GameStateValidator()(gameState)
          .validateAbilityUse(s.owners(0), abilityId, UseData(s.p(0)(1).character.id))
      }
    }

    "not be able to use switch on enemies" in {
      assertCommandFailure {
        GameStateValidator()(gameState)
          .validateAbilityUse(s.owners(0), abilityId, UseData(s.defaultEnemy.id))
      }
    }

    "not be able to use switch with himself" in {
      val ngs = gameState.teleportCharacter(s.defaultCharacter.id, HexCoordinates(2, 0))

      ngs.abilityById(abilityId).targetsInRange(ngs) should not contain s.defaultCharacter.parentCellOpt(
        ngs
      ).get.coordinates

      assertCommandFailure {
        GameStateValidator()(ngs)
          .validateAbilityUse(s.owners(0), abilityId, UseData(s.defaultCharacter.id))
      }
    }

    "not be able to use switch on character outside map" in {
      val ngs = gameState
        .teleportCharacter(s.defaultCharacter.id, HexCoordinates(2, 0))
        .removeCharacterFromMap(s.p(0)(1).character.id)

      assertCommandFailure {
        GameStateValidator()(ngs)
          .validateAbilityUse(s.owners(0), abilityId, UseData(s.p(0)(1).character.id))
      }
    }
  }
}
