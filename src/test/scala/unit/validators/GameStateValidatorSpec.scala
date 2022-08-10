package unit.validators

import com.tosware.NKM.models.GameStateValidator
import com.tosware.NKM.models.game._
import com.tosware.NKM.models.game.effects._
import com.tosware.NKM.models.game.hex.HexCoordinates
import com.tosware.NKM.models.game.hex.HexUtils.CoordinateSeq
import com.tosware.NKM.providers.HexMapProvider.TestHexMapName
import helpers.TestUtils
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class GameStateValidatorSpec
  extends AnyWordSpecLike
    with Matchers
    with TestUtils
    {
  val metadata = CharacterMetadata.empty().copy(initialSpeed = 3, initialBasicAttackRange = 1)
  val gameState = getTestGameState(TestHexMapName.Simple2v2, Seq(
    Seq(metadata.copy(name = "Empty1"), metadata.copy(name = "Empty2")),
    Seq(metadata.copy(name = "Empty3"), metadata.copy(name = "Empty4")),
  ))

  def characterIdOnPoint(hexCoordinates: HexCoordinates) = gameState.hexMap.get.getCell(hexCoordinates).get.characterId.get

  val p0FirstCharacterId = characterIdOnPoint(HexCoordinates(0, 0))
  val p0SecondCharacterId = characterIdOnPoint(HexCoordinates(-1, 0))

  val p1FirstCharacterId = characterIdOnPoint(HexCoordinates(3, 0))
  val p1SecondCharacterId = characterIdOnPoint(HexCoordinates(4, 0))

  private val validator = GameStateValidator()(gameState)

  "GameStateValidator" must {
    "validate moving characters and" when {
      "allow move within speed range" in {
        val result = validator.validateBasicMoveCharacter(gameState.players(0).id,
          CoordinateSeq((0, 0), (1, 0)),
          p0FirstCharacterId
        )
        assertCommandSuccess(result)
      }

      "allow move over friendly characters" in {
        val result = validator.validateBasicMoveCharacter(gameState.players(0).id,
          CoordinateSeq((0, 0), (-1, 0), (-2, 0)),
          p0FirstCharacterId
        )
        assertCommandSuccess(result)
      }

      "disallow if character is not on the map" in {
        val newGameState = gameState.removeCharacterFromMap(p0FirstCharacterId)(gameState.id)
        val result =  GameStateValidator()(newGameState).validateBasicMoveCharacter(gameState.players(0).id,
          CoordinateSeq((0, 0), (1, 0)),
          p0FirstCharacterId
        )
        assertCommandFailure(result)
      }

      "disallow if character is grounded" in {
        val newGameState = gameState.addEffect(p0FirstCharacterId, GroundEffect(1))(gameState.id)

        val result = GameStateValidator()(newGameState).validateBasicMoveCharacter(
          gameState.players(0).id,
          CoordinateSeq((0, 0), (1, 0)),
          p0FirstCharacterId
        )
        assertCommandFailure(result)
      }

      "disallow if character is snared" in {
        val newGameState = gameState.addEffect(p0FirstCharacterId, SnareEffect(1))(gameState.id)

        val result = GameStateValidator()(newGameState).validateBasicMoveCharacter(
          gameState.players(0).id,
          CoordinateSeq((0, 0), (1, 0)),
          p0FirstCharacterId
        )
        assertCommandFailure(result)
      }

      "disallow if character is stunned" in {
        val newGameState = gameState.addEffect(p0FirstCharacterId, StunEffect(1))(gameState.id)

        val result = GameStateValidator()(newGameState).validateBasicMoveCharacter(
          gameState.players(0).id,
          CoordinateSeq((0, 0), (1, 0)),
          p0FirstCharacterId
        )
        assertCommandFailure(result)
      }

      "disallow empty moves" in {
        val result = validator.validateBasicMoveCharacter(gameState.players(0).id,
          CoordinateSeq(),
          p0FirstCharacterId
        )
        assertCommandFailure(result)

        val result2 = validator.validateBasicMoveCharacter(gameState.players(0).id,
          CoordinateSeq((0, 0)),
          p0FirstCharacterId
        )
        assertCommandFailure(result2)
      }

      "disallow move from other cell than characters" in {
        val result = validator.validateBasicMoveCharacter(gameState.players(0).id,
          CoordinateSeq((1, 0), (2, 0)),
          p0FirstCharacterId
        )
        assertCommandFailure(result)
      }

      "disallow move outside of turn" in {
        val result = validator.validateBasicMoveCharacter(gameState.players(1).id,
          CoordinateSeq((3, 0), (2, 0)),
          p1FirstCharacterId
        )
        assertCommandFailure(result)
      }

      "disallow move foreign characters" in {
        val result = validator.validateBasicMoveCharacter(gameState.players(0).id,
          CoordinateSeq((3, 0), (2, 0)),
          p1FirstCharacterId
        )
        assertCommandFailure(result)
      }


      "disallow move above speed range" in {
        val result = validator.validateBasicMoveCharacter(gameState.players(0).id,
          CoordinateSeq((0, 0), (1, 0), (2, 0), (2, 1), (2, 2)),
          p0FirstCharacterId
        )
        assertCommandFailure(result)
      }

      "disallow move into the same position" in {
        val result = validator.validateBasicMoveCharacter(gameState.players(0).id,
          CoordinateSeq((0, 0), (1, 0), (0, 0)),
          p0FirstCharacterId
        )
        assertCommandFailure(result)
      }

      "disallow move that visits another cell more than once" in {
        val result = validator.validateBasicMoveCharacter(gameState.players(0).id,
          CoordinateSeq((0, 0), (1, 0), (2, 0), (1, 0)),
          p0FirstCharacterId
        )
        assertCommandFailure(result)
      }

      "disallow move if character already moved" in {
        val newGameState = gameState.basicMoveCharacter(gameState.players(0).id,
          CoordinateSeq((0, 0), (1, 0)),
          p0FirstCharacterId)
        val result = GameStateValidator()(newGameState).validateBasicMoveCharacter(gameState.players(0).id,
          CoordinateSeq((1, 0), (0, 0)),
          p0FirstCharacterId
        )
        assertCommandFailure(result)
      }

      "disallow move if character used ultimate ability" in {
        fail()
      }

      "disallow move if other character took action in turn" in {
          val newGameState = gameState.takeActionWithCharacter("test_nonexistent_id")

          val result = GameStateValidator()(newGameState).validateBasicMoveCharacter(gameState.players(0).id,
            CoordinateSeq((0, 0), (1, 0)),
            p0FirstCharacterId
          )
          assertCommandFailure(result)
      }

      "disallow move if there is an obstacle on path" in {
        val result = validator.validateBasicMoveCharacter(gameState.players(0).id,
          CoordinateSeq((0, 0), (1, -1), (1, 0)),
          p0FirstCharacterId
        )
        assertCommandFailure(result)
      }

      "disallow move if cell at the end is not free to move" in {
        val result = validator.validateBasicMoveCharacter(gameState.players(0).id,
          CoordinateSeq((0, 0), (1, -1)),
          p0FirstCharacterId
        )
        assertCommandFailure(result)
      }

      "disallow move if moving to not adjacent cell" in {
        val result = validator.validateBasicMoveCharacter(gameState.players(0).id,
          CoordinateSeq((0, 0), (0, 2)),
          p0FirstCharacterId
        )
        assertCommandFailure(result)
      }
    }

    "validate attacking characters and" when {
      val moveGameState = gameState.basicMoveCharacter(gameState.players(0).id, CoordinateSeq((0, 0), (1, 0), (2, 0)), p0FirstCharacterId)

      "allow if character is in attack range" in {
        val result = GameStateValidator()(moveGameState).validateBasicAttackCharacter(gameState.players(0).id,
          p0FirstCharacterId,
          p1FirstCharacterId,
        )

        assertCommandSuccess(result)
      }

      "disallow if character is not on the map" in {
        val newGameState = moveGameState.removeCharacterFromMap(p0FirstCharacterId)(gameState.id)

        val result = GameStateValidator()(newGameState).validateBasicAttackCharacter(gameState.players(0).id,
          p0FirstCharacterId,
          p1FirstCharacterId,
        )
        assertCommandFailure(result)
      }

      "allow if character is grounded" in {
        val newGameState = moveGameState.addEffect(p0FirstCharacterId, GroundEffect(1))(gameState.id)

        val result = GameStateValidator()(newGameState).validateBasicAttackCharacter(
          gameState.players(0).id,
          p0FirstCharacterId,
          p1FirstCharacterId,
        )
        assertCommandSuccess(result)
      }

      "allow if character is snared" in {
        val newGameState = moveGameState.addEffect(p0FirstCharacterId, SnareEffect(1))(gameState.id)

        val result = GameStateValidator()(newGameState).validateBasicAttackCharacter(
          gameState.players(0).id,
          p0FirstCharacterId,
          p1FirstCharacterId,
        )
        assertCommandSuccess(result)
      }

      "disallow character is stunned" in {
        val newGameState = moveGameState.addEffect(p0FirstCharacterId, StunEffect(1))(gameState.id)

        val result = GameStateValidator()(newGameState).validateBasicAttackCharacter(
          gameState.players(0).id,
          p0FirstCharacterId,
          p1FirstCharacterId,
        )
        assertCommandFailure(result)
      }

      "disallow character is disarmed" in {
        val newGameState = moveGameState.addEffect(p0FirstCharacterId, DisarmEffect(1))(gameState.id)

        val result = GameStateValidator()(newGameState).validateBasicAttackCharacter(
          gameState.players(0).id,
          p0FirstCharacterId,
          p1FirstCharacterId,
        )
        assertCommandFailure(result)
      }

      "disallow if character is not in attack range" in {
        val result = validator.validateBasicAttackCharacter(gameState.players(0).id,
          p0FirstCharacterId,
          p1FirstCharacterId,
        )

        assertCommandFailure(result)
      }

      "disallow if character already basic attacked" in {
        val newState = moveGameState.basicAttack(p0FirstCharacterId, p1FirstCharacterId)
        val result = GameStateValidator()(newState).validateBasicAttackCharacter(gameState.players(0).id,
          p0FirstCharacterId,
          p1FirstCharacterId,
        )
        assertCommandFailure(result)
      }

      "disallow if character used ultimate ability" in {
        fail()
      }

      "disallow move if other character took action in turn" in {
        val newGameState = moveGameState.takeActionWithCharacter("test_nonexistent_id")

        val result = GameStateValidator()(newGameState).validateBasicAttackCharacter(
          gameState.players(0).id,
          p0FirstCharacterId,
          p1FirstCharacterId,
        )
        assertCommandFailure(result)
      }
    }
  }
}
