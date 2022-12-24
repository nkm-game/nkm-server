package unit.validators

import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.abilities.hecate.PowerOfExistence
import com.tosware.nkm.models.game.effects._
import com.tosware.nkm.models.game.hex.{HexCoordinates, NkmUtils}
import com.tosware.nkm.models.game.hex.HexUtils.CoordinateSeq
import com.tosware.nkm.providers.HexMapProvider.TestHexMapName
import helpers.{TestUtils, scenarios}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import com.tosware.nkm.models.game.hex.HexUtils._

class GameStateValidatorSpec
  extends AnyWordSpecLike
    with Matchers
    with TestUtils
{
  private val metadata = CharacterMetadata.empty()
    .copy(
      initialSpeed = 3,
      initialBasicAttackRange = 1,
      initialAbilitiesMetadataIds = Seq(PowerOfExistence.metadata.id)
    )

  private val s = scenarios.Simple2v2TestScenario(metadata)
  private implicit val gameState: GameState = s.gameState

  private val wallMeleeScenario = scenarios.Simple2v2TestScenario(
    metadata.copy(attackType = AttackType.Melee, initialBasicAttackRange = 4),
    TestHexMapName.Simple2v2Wall
  )

  private val wallRangedScenario = scenarios.Simple2v2TestScenario(
    metadata.copy(attackType = AttackType.Ranged, initialBasicAttackRange = 4),
    TestHexMapName.Simple2v2Wall
  )

  private val abilityId = s.characters.p0First.state.abilities.head.id
  private val validator = GameStateValidator()(gameState)

  "GameStateValidator" must {
    "pass sanity check" in {
      gameState.characters.count(_.isOnMap(gameState)) should be (s.gameState.characters.size)
      gameState.hexMap.get.cells.whereCharacters.size should be (s.gameState.characters.size)
    }
    "validate moving characters and" when {
      "allow move within speed range" in {
        assertCommandSuccess {
          validator.validateBasicMoveCharacter(gameState.players(0).id,
            CoordinateSeq((0, 0), (1, 0)),
            s.characters.p0First.id
          )
        }
      }

      "allow move over friendly characters" in {
        assertCommandSuccess {
          validator.validateBasicMoveCharacter(gameState.players(0).id,
            CoordinateSeq((0, 0), (-1, 0), (-2, 0)),
            s.characters.p0First.id
          )
        }

        val newGameState = gameState.basicMoveCharacter( s.characters.p0First.id, CoordinateSeq((0, 0), (-1, 0), (-2, 0)))
        newGameState.characters.count(_.isOnMap(newGameState)) should be (s.gameState.characters.size)
        newGameState.hexMap.get.cells.whereCharacters.size should be (s.gameState.characters.size)
      }

      "disallow if character is not on the map" in {
        val newGameState = gameState.removeCharacterFromMap(s.characters.p0First.id)(random, gameState.id)

        assertCommandFailure {
          GameStateValidator()(newGameState).validateBasicMoveCharacter(gameState.players(0).id,
            CoordinateSeq((0, 0), (1, 0)),
            s.characters.p0First.id
          )
        }
      }

      "disallow if character is grounded" in {
        val newGameState = gameState.addEffect(s.characters.p0First.id, Ground(NkmUtils.randomUUID(), 1))(random, gameState.id)

        assertCommandFailure {
          GameStateValidator()(newGameState).validateBasicMoveCharacter(
            gameState.players(0).id,
            CoordinateSeq((0, 0), (1, 0)),
            s.characters.p0First.id
          )
        }
      }

      "disallow if character is snared" in {
        val newGameState = gameState.addEffect(s.characters.p0First.id, Snare(NkmUtils.randomUUID(), 1))(random, gameState.id)

        assertCommandFailure {
          GameStateValidator()(newGameState).validateBasicMoveCharacter(
            gameState.players(0).id,
            CoordinateSeq((0, 0), (1, 0)),
            s.characters.p0First.id
          )
        }
      }

      "disallow if character is stunned" in {
        val newGameState = gameState.addEffect(s.characters.p0First.id, Stun(NkmUtils.randomUUID(), 1))(random, gameState.id)

        assertCommandFailure {
          GameStateValidator()(newGameState).validateBasicMoveCharacter(
            gameState.players(0).id,
            CoordinateSeq((0, 0), (1, 0)),
            s.characters.p0First.id
          )
        }
      }

      "disallow empty moves" in {
        assertCommandFailure {
          validator.validateBasicMoveCharacter(gameState.players(0).id,
            CoordinateSeq(),
            s.characters.p0First.id
          )
        }
        assertCommandFailure {
          validator.validateBasicMoveCharacter(gameState.players(0).id,
            CoordinateSeq((0, 0)),
            s.characters.p0First.id
          )
        }
      }

      "disallow move from other cell than characters" in {
        assertCommandFailure {
          validator.validateBasicMoveCharacter(gameState.players(0).id,
            CoordinateSeq((1, 0), (2, 0)),
            s.characters.p0First.id
          )
        }
      }

      "disallow move outside of turn" in {
        assertCommandFailure {
          validator.validateBasicMoveCharacter(gameState.players(1).id,
            CoordinateSeq((3, 0), (2, 0)),
            s.characters.p1First.id
          )
        }
      }

      "disallow move foreign characters" in {
        assertCommandFailure {
          validator.validateBasicMoveCharacter(gameState.players(0).id,
            CoordinateSeq((3, 0), (2, 0)),
            s.characters.p1First.id
          )
        }
      }


      "disallow move above speed range" in {
        assertCommandFailure {
          validator.validateBasicMoveCharacter(gameState.players(0).id,
            CoordinateSeq((0, 0), (1, 0), (2, 0), (2, 1), (1, 1)),
            s.characters.p0First.id
          )
        }
      }

      "disallow move into the same position" in {
        assertCommandFailure {
          validator.validateBasicMoveCharacter(gameState.players(0).id,
            CoordinateSeq((0, 0), (1, 0), (0, 0)),
            s.characters.p0First.id
          )
        }
      }

      "disallow move that visits another cell more than once" in {
        assertCommandFailure {
          validator.validateBasicMoveCharacter(gameState.players(0).id,
            CoordinateSeq((0, 0), (1, 0), (2, 0), (1, 0)),
            s.characters.p0First.id
          )
        }
      }

      "disallow move if character already moved" in {
        val newGameState = gameState.basicMoveCharacter(
          s.characters.p0First.id,
          CoordinateSeq((0, 0), (1, 0)))

        assertCommandFailure {
          GameStateValidator()(newGameState).validateBasicMoveCharacter(gameState.players(0).id,
            CoordinateSeq((1, 0), (0, 0)),
            s.characters.p0First.id
          )
        }
      }

      "disallow move if character used ultimate ability" in {
        val newGameState = gameState.useAbilityWithoutTarget(abilityId)

        assertCommandFailure {
          GameStateValidator()(newGameState).validateBasicMoveCharacter(gameState.players(0).id,
            CoordinateSeq((0, 0), (1, 0)),
            s.characters.p0First.id
          )
        }
      }

      "disallow move if other character took action in turn" in {
        val newGameState = gameState.takeActionWithCharacter("test_nonexistent_id")

        val result = GameStateValidator()(newGameState).validateBasicMoveCharacter(gameState.players(0).id,
          CoordinateSeq((0, 0), (1, 0)),
          s.characters.p0First.id
        )
        assertCommandFailure(result)
      }

      "disallow move if there is an obstacle on path" in {
        val result = validator.validateBasicMoveCharacter(gameState.players(0).id,
          CoordinateSeq((0, 0), (1, -1), (1, 0)),
          s.characters.p0First.id
        )
        assertCommandFailure(result)
      }

      "disallow move if cell at the end is not free to move" in {
        val result = validator.validateBasicMoveCharacter(gameState.players(0).id,
          CoordinateSeq((0, 0), (1, -1)),
          s.characters.p0First.id
        )
        assertCommandFailure(result)
      }

      "disallow move if moving to not adjacent cell" in {
        val result = validator.validateBasicMoveCharacter(gameState.players(0).id,
          CoordinateSeq((0, 0), (0, 2)),
          s.characters.p0First.id
        )
        assertCommandFailure(result)
      }
    }

    "validate attacking characters and" when {
      val moveGameState = gameState.teleportCharacter(s.characters.p0First.id, HexCoordinates(2, 0))(random, gameState.id)

      "allow if character is in attack range without obstacles" in {
        assertCommandSuccess {
          GameStateValidator()(moveGameState).validateBasicAttackCharacter(s.characters.p0First.owner.id,
            s.characters.p0First.id,
            s.characters.p1First.id,
          )
        }
      }

      "allow over wall if character is ranged" in {
        val s = wallRangedScenario
        implicit val gameState: GameState = s.gameState
        assertCommandSuccess {
          GameStateValidator().validateBasicAttackCharacter(s.characters.p0First.owner.id,
            s.characters.p0First.id,
            s.characters.p1First.id,
          )
        }
      }

      "disallow over wall if character is melee" in {
        val s = wallMeleeScenario
        implicit val gameState: GameState = s.gameState
        assertCommandFailure {
          GameStateValidator().validateBasicAttackCharacter(s.characters.p0First.owner.id,
            s.characters.p0First.id,
            s.characters.p1First.id,
          )
        }
      }

      "allow over character if character is ranged" in {
        val s = wallRangedScenario
        implicit val gameState: GameState = s.gameState
        assertCommandSuccess {
          GameStateValidator().validateBasicAttackCharacter(s.characters.p0First.owner.id,
            s.characters.p0First.id,
            s.characters.p1Second.id,
          )
        }
      }

      "disallow over character if character is melee" in {
        val s = wallMeleeScenario
        implicit val gameState: GameState = s.gameState
        assertCommandFailure {
          GameStateValidator().validateBasicAttackCharacter(s.characters.p0First.owner.id,
            s.characters.p0First.id,
            s.characters.p1Second.id,
          )
        }
      }

      "disallow if character is not on the map" in {
        val newGameState = moveGameState.removeCharacterFromMap(s.characters.p0First.id)(random, gameState.id)

        assertCommandFailure {
          GameStateValidator()(newGameState).validateBasicAttackCharacter(gameState.players(0).id,
            s.characters.p0First.id,
            s.characters.p1First.id,
          )
        }
      }

      "allow if character is grounded" in {
        val newGameState = moveGameState.addEffect(s.characters.p0First.id, Ground(NkmUtils.randomUUID(), 1))(random, gameState.id)

        assertCommandSuccess {
          GameStateValidator()(newGameState).validateBasicAttackCharacter(
            gameState.players(0).id,
            s.characters.p0First.id,
            s.characters.p1First.id,
          )
        }
      }

      "allow if character is snared" in {
        val newGameState = moveGameState.addEffect(s.characters.p0First.id, Snare(NkmUtils.randomUUID(), 1))(random, gameState.id)

        assertCommandSuccess {
          GameStateValidator()(newGameState).validateBasicAttackCharacter(
            gameState.players(0).id,
            s.characters.p0First.id,
            s.characters.p1First.id,
          )
        }
      }

      "disallow character is stunned" in {
        val newGameState = moveGameState.addEffect(s.characters.p0First.id, Stun(NkmUtils.randomUUID(), 1))(random, gameState.id)

        assertCommandFailure {
          GameStateValidator()(newGameState).validateBasicAttackCharacter(
            gameState.players(0).id,
            s.characters.p0First.id,
            s.characters.p1First.id,
          )
        }
      }

      "disallow character is disarmed" in {
        val newGameState = moveGameState.addEffect(s.characters.p0First.id, Disarm(NkmUtils.randomUUID(), 1))(random, gameState.id)

        assertCommandFailure {
          GameStateValidator()(newGameState).validateBasicAttackCharacter(
            gameState.players(0).id,
            s.characters.p0First.id,
            s.characters.p1First.id,
          )
        }
      }

      "disallow if character is not in attack range" in {
        assertCommandFailure {
          validator.validateBasicAttackCharacter(gameState.players(0).id,
            s.characters.p0First.id,
            s.characters.p1First.id,
          )
        }
      }

      "disallow if character already basic attacked" in {
        val newState = moveGameState.basicAttack(s.characters.p0First.id, s.characters.p1First.id)

        assertCommandFailure {
          GameStateValidator()(newState).validateBasicAttackCharacter(gameState.players(0).id,
            s.characters.p0First.id,
            s.characters.p1First.id,
          )
        }
      }

      "disallow if character used ultimate ability" in {
        val newGameState = moveGameState.useAbilityWithoutTarget(abilityId)

        assertCommandFailure {
          GameStateValidator()(newGameState).validateBasicAttackCharacter(
            gameState.players(0).id,
            s.characters.p0First.id,
            s.characters.p1First.id,
          )
        }
      }

      "disallow move if other character took action in turn" in {
        val newGameState = moveGameState.takeActionWithCharacter("test_nonexistent_id")

        assertCommandFailure {
          GameStateValidator()(newGameState).validateBasicAttackCharacter(
            gameState.players(0).id,
            s.characters.p0First.id,
            s.characters.p1First.id,
          )
        }
      }
    }

    "validate using abilities" when {
      "allow use of ability" in {
        val incrementGameState = gameState.incrementPhase(4)
        assertCommandSuccess {
          GameStateValidator()(incrementGameState)
            .validateAbilityUseWithoutTarget(s.characters.p0First.owner.id, abilityId)
        }
      }

      "disallow use of ultimate ability before phase 4" in {
        assertCommandFailure {
          GameStateValidator()(gameState)
            .validateAbilityUseWithoutTarget(s.characters.p0First.owner.id, abilityId)
        }

        val increment3GameState = gameState.incrementPhase(3)
        assertCommandFailure {
          GameStateValidator()(increment3GameState)
            .validateAbilityUseWithoutTarget(s.characters.p0First.owner.id, abilityId)
        }

        val increment4GameState = gameState.incrementPhase(4)
        assertCommandSuccess {
          GameStateValidator()(increment4GameState)
            .validateAbilityUseWithoutTarget(s.characters.p0First.owner.id, abilityId)
        }
      }

      "disallow use of ability on cooldown" in {
        val incrementGameState = gameState.incrementPhase(4)
        val newGameState = incrementGameState.useAbilityWithoutTarget(abilityId)
          .endTurn()
          .passTurn(s.characters.p1First.id)
        newGameState.abilityById(abilityId).get.state(newGameState).cooldown should be > 0


        assertCommandFailure {
          GameStateValidator()(newGameState)
            .validateAbilityUseWithoutTarget(s.characters.p0First.owner.id, abilityId)
        }
      }
      "disallow using ability another time in phase" in {
        val incrementGameState = gameState.incrementPhase(4)
        val newGameState = incrementGameState.useAbilityWithoutTarget(abilityId)
          .endTurn()
          .passTurn(s.characters.p1First.id)
          .decrementAbilityCooldown(abilityId, 999)
        newGameState.abilityById(abilityId).get.state(newGameState).cooldown should be (0)


        assertCommandFailure {
          GameStateValidator()(newGameState)
            .validateAbilityUseWithoutTarget(s.characters.p0First.owner.id, abilityId)
        }
      }
    }
  }
}
