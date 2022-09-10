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
    "validate moving characters and" when {
      "allow move within speed range" in {
        val result = validator.validateBasicMoveCharacter(gameState.players(0).id,
          CoordinateSeq((0, 0), (1, 0)),
          s.characters.p0First.id
        )
        assertCommandSuccess(result)
      }

      "allow move over friendly characters" in {
        val result = validator.validateBasicMoveCharacter(gameState.players(0).id,
          CoordinateSeq((0, 0), (-1, 0), (-2, 0)),
          s.characters.p0First.id
        )
        assertCommandSuccess(result)
      }

      "disallow if character is not on the map" in {
        val newGameState = gameState.removeCharacterFromMap(s.characters.p0First.id)(random, gameState.id)
        val result =  GameStateValidator()(newGameState).validateBasicMoveCharacter(gameState.players(0).id,
          CoordinateSeq((0, 0), (1, 0)),
          s.characters.p0First.id
        )
        assertCommandFailure(result)
      }

      "disallow if character is grounded" in {
        val newGameState = gameState.addEffect(s.characters.p0First.id, Ground(NkmUtils.randomUUID(), 1))(random, gameState.id)

        val result = GameStateValidator()(newGameState).validateBasicMoveCharacter(
          gameState.players(0).id,
          CoordinateSeq((0, 0), (1, 0)),
          s.characters.p0First.id
        )
        assertCommandFailure(result)
      }

      "disallow if character is snared" in {
        val newGameState = gameState.addEffect(s.characters.p0First.id, Snare(NkmUtils.randomUUID(), 1))(random, gameState.id)

        val result = GameStateValidator()(newGameState).validateBasicMoveCharacter(
          gameState.players(0).id,
          CoordinateSeq((0, 0), (1, 0)),
          s.characters.p0First.id
        )
        assertCommandFailure(result)
      }

      "disallow if character is stunned" in {
        val newGameState = gameState.addEffect(s.characters.p0First.id, Stun(NkmUtils.randomUUID(), 1))(random, gameState.id)

        val result = GameStateValidator()(newGameState).validateBasicMoveCharacter(
          gameState.players(0).id,
          CoordinateSeq((0, 0), (1, 0)),
          s.characters.p0First.id
        )
        assertCommandFailure(result)
      }

      "disallow empty moves" in {
        val result = validator.validateBasicMoveCharacter(gameState.players(0).id,
          CoordinateSeq(),
          s.characters.p0First.id
        )
        assertCommandFailure(result)

        val result2 = validator.validateBasicMoveCharacter(gameState.players(0).id,
          CoordinateSeq((0, 0)),
          s.characters.p0First.id
        )
        assertCommandFailure(result2)
      }

      "disallow move from other cell than characters" in {
        val result = validator.validateBasicMoveCharacter(gameState.players(0).id,
          CoordinateSeq((1, 0), (2, 0)),
          s.characters.p0First.id
        )
        assertCommandFailure(result)
      }

      "disallow move outside of turn" in {
        val result = validator.validateBasicMoveCharacter(gameState.players(1).id,
          CoordinateSeq((3, 0), (2, 0)),
          s.characters.p1First.id
        )
        assertCommandFailure(result)
      }

      "disallow move foreign characters" in {
        val result = validator.validateBasicMoveCharacter(gameState.players(0).id,
          CoordinateSeq((3, 0), (2, 0)),
          s.characters.p1First.id
        )
        assertCommandFailure(result)
      }


      "disallow move above speed range" in {
        val result = validator.validateBasicMoveCharacter(gameState.players(0).id,
          CoordinateSeq((0, 0), (1, 0), (2, 0), (2, 1), (1, 1)),
          s.characters.p0First.id
        )
        assertCommandFailure(result)
      }

      "disallow move into the same position" in {
        val result = validator.validateBasicMoveCharacter(gameState.players(0).id,
          CoordinateSeq((0, 0), (1, 0), (0, 0)),
          s.characters.p0First.id
        )
        assertCommandFailure(result)
      }

      "disallow move that visits another cell more than once" in {
        val result = validator.validateBasicMoveCharacter(gameState.players(0).id,
          CoordinateSeq((0, 0), (1, 0), (2, 0), (1, 0)),
          s.characters.p0First.id
        )
        assertCommandFailure(result)
      }

      "disallow move if character already moved" in {
        val newGameState = gameState.basicMoveCharacter(
          s.characters.p0First.id,
          CoordinateSeq((0, 0), (1, 0)))
        val result = GameStateValidator()(newGameState).validateBasicMoveCharacter(gameState.players(0).id,
          CoordinateSeq((1, 0), (0, 0)),
          s.characters.p0First.id
        )
        assertCommandFailure(result)
      }

      "disallow move if character used ultimate ability" in {
        val newGameState = gameState.useAbilityWithoutTarget(abilityId)
        val result = GameStateValidator()(newGameState).validateBasicMoveCharacter(gameState.players(0).id,
          CoordinateSeq((0, 0), (1, 0)),
          s.characters.p0First.id
        )
        assertCommandFailure(result)
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
        val result = GameStateValidator()(moveGameState).validateBasicAttackCharacter(s.characters.p0First.owner.id,
          s.characters.p0First.id,
          s.characters.p1First.id,
        )

        assertCommandSuccess(result)
      }

      "allow over wall if character is ranged" in {
        val s = wallRangedScenario
        implicit val gameState: GameState = s.gameState
        val result = GameStateValidator().validateBasicAttackCharacter(s.characters.p0First.owner.id,
          s.characters.p0First.id,
          s.characters.p1First.id,
        )

        assertCommandSuccess(result)
      }

      "disallow over wall if character is melee" in {
        val s = wallMeleeScenario
        implicit val gameState: GameState = s.gameState
        val result = GameStateValidator().validateBasicAttackCharacter(s.characters.p0First.owner.id,
          s.characters.p0First.id,
          s.characters.p1First.id,
        )

        assertCommandFailure(result)
      }

      "allow over character if character is ranged" in {
        val s = wallRangedScenario
        implicit val gameState: GameState = s.gameState
        val result = GameStateValidator().validateBasicAttackCharacter(s.characters.p0First.owner.id,
          s.characters.p0First.id,
          s.characters.p1Second.id,
        )

        assertCommandSuccess(result)
      }

      "disallow over character if character is melee" in {
        val s = wallMeleeScenario
        implicit val gameState: GameState = s.gameState
        val result = GameStateValidator().validateBasicAttackCharacter(s.characters.p0First.owner.id,
          s.characters.p0First.id,
          s.characters.p1Second.id,
        )

        assertCommandFailure(result)
      }

      "disallow if character is not on the map" in {
        val newGameState = moveGameState.removeCharacterFromMap(s.characters.p0First.id)(random, gameState.id)

        val result = GameStateValidator()(newGameState).validateBasicAttackCharacter(gameState.players(0).id,
          s.characters.p0First.id,
          s.characters.p1First.id,
        )
        assertCommandFailure(result)
      }

      "allow if character is grounded" in {
        val newGameState = moveGameState.addEffect(s.characters.p0First.id, Ground(NkmUtils.randomUUID(), 1))(random, gameState.id)

        val result = GameStateValidator()(newGameState).validateBasicAttackCharacter(
          gameState.players(0).id,
          s.characters.p0First.id,
          s.characters.p1First.id,
        )
        assertCommandSuccess(result)
      }

      "allow if character is snared" in {
        val newGameState = moveGameState.addEffect(s.characters.p0First.id, Snare(NkmUtils.randomUUID(), 1))(random, gameState.id)

        val result = GameStateValidator()(newGameState).validateBasicAttackCharacter(
          gameState.players(0).id,
          s.characters.p0First.id,
          s.characters.p1First.id,
        )
        assertCommandSuccess(result)
      }

      "disallow character is stunned" in {
        val newGameState = moveGameState.addEffect(s.characters.p0First.id, Stun(NkmUtils.randomUUID(), 1))(random, gameState.id)

        val result = GameStateValidator()(newGameState).validateBasicAttackCharacter(
          gameState.players(0).id,
          s.characters.p0First.id,
          s.characters.p1First.id,
        )
        assertCommandFailure(result)
      }

      "disallow character is disarmed" in {
        val newGameState = moveGameState.addEffect(s.characters.p0First.id, Disarm(NkmUtils.randomUUID(), 1))(random, gameState.id)

        val result = GameStateValidator()(newGameState).validateBasicAttackCharacter(
          gameState.players(0).id,
          s.characters.p0First.id,
          s.characters.p1First.id,
        )
        assertCommandFailure(result)
      }

      "disallow if character is not in attack range" in {
        val result = validator.validateBasicAttackCharacter(gameState.players(0).id,
          s.characters.p0First.id,
          s.characters.p1First.id,
        )

        assertCommandFailure(result)
      }

      "disallow if character already basic attacked" in {
        val newState = moveGameState.basicAttack(s.characters.p0First.id, s.characters.p1First.id)
        val result = GameStateValidator()(newState).validateBasicAttackCharacter(gameState.players(0).id,
          s.characters.p0First.id,
          s.characters.p1First.id,
        )
        assertCommandFailure(result)
      }

      "disallow if character used ultimate ability" in {
        val newGameState = moveGameState.useAbilityWithoutTarget(abilityId)
        val result = GameStateValidator()(newGameState).validateBasicAttackCharacter(
          gameState.players(0).id,
          s.characters.p0First.id,
          s.characters.p1First.id,
        )
        assertCommandFailure(result)
      }

      "disallow move if other character took action in turn" in {
        val newGameState = moveGameState.takeActionWithCharacter("test_nonexistent_id")

        val result = GameStateValidator()(newGameState).validateBasicAttackCharacter(
          gameState.players(0).id,
          s.characters.p0First.id,
          s.characters.p1First.id,
        )
        assertCommandFailure(result)
      }
    }
  }
}
