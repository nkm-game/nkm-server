package unit.validators

import com.tosware.NKM.models.CommandResponse._
import com.tosware.NKM.models.GameStateValidator
import com.tosware.NKM.models.game.PickType.BlindPick
import com.tosware.NKM.models.game._
import com.tosware.NKM.models.game.effects._
import com.tosware.NKM.models.game.hex.HexCellType._
import com.tosware.NKM.models.game.hex.HexUtils.CoordinateSeq
import com.tosware.NKM.models.game.hex._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.slf4j.{Logger, LoggerFactory}

import scala.util.Random

class GameStateValidatorSpec
  extends AnyWordSpecLike
    with Matchers {
  implicit val random: Random = new Random()
  val logger: Logger = LoggerFactory.getLogger(getClass)
  val hexParams: Set[Any] = Set(
      (-2, 2, Wall),
      (-1, 2, Wall),
      (0, 2, Wall),
      (1, 2, Wall),
      (2, 2, Wall),
      (3, 2, Wall),
      (-1, 1),
      (0, 1),
      (1, 1),
      (2, 1),
      (3, 1),
      (0, 0, SpawnPoint, 0),
      (1, 0),
      (2, 0),
      (3, 0, SpawnPoint, 1),
      (0, -1, Wall),
      (1, -1, Wall),
      (2, -1, Wall),
      (3, -1, Wall),
      (4, -1, Wall),
    )

  val cells: Set[HexCell] = HexUtils.hexCellParamsToCells(hexParams)

  private val hexMap = HexMap("test", cells)

  def spawnCoordinates(playerIndex: Int) = hexMap.getSpawnPointsByNumber(playerIndex).head.coordinates

  private val playerIds = Seq("p0", "p1")
  private val gameStateDeps = GameStartDependencies(
    players = playerIds.map(n => Player(n)),
    hexMap = hexMap,
    pickType = BlindPick,
    numberOfBansPerPlayer = 0,
    numberOfCharactersPerPlayer = 1,
    charactersMetadata = Set(NKMCharacterMetadata.empty().copy(initialSpeed = 3, initialBasicAttackRange = 1)),
    clockConfig = ClockConfig.empty()
  )

  private val placingGameState = GameState.empty("test")
    .startGame(gameStateDeps)
    .blindPick(playerIds(0), Set(gameStateDeps.charactersMetadata.head.id))
    .blindPick(playerIds(1), Set(gameStateDeps.charactersMetadata.head.id))
    .startPlacingCharacters()

  private def getCharacterId(playerIndex: Int) = placingGameState.players(playerIndex).characters.head.id

  private val runningGameState = placingGameState
    .placeCharacters(playerIds(0), Map(spawnCoordinates(0) -> getCharacterId(0)))
    .placeCharacters(playerIds(1), Map(spawnCoordinates(1) -> getCharacterId(1)))

  private val validator = GameStateValidator()(runningGameState)

  private def assertCommandSuccess(c: CommandResponse): Unit = c match {
    case Success(_) =>
    case Failure(m) =>
      logger.error(m)
      fail()
  }

  private def assertCommandFailure(c: CommandResponse): Unit = c match {
    case Success(_) => fail()
    case Failure(m) => logger.info(m)
  }

  "GameStateValidator" must {
    logger.info(hexMap.toTextUi)
    "validate moving characters and" when {
      "allow move within speed range" in {
        val result = validator.validateBasicMoveCharacter(playerIds(0),
          CoordinateSeq((0, 0), (1, 0)),
          getCharacterId(0)
        )
        assertCommandSuccess(result)
      }

      "disallow if character is not on the map" in {
        val newGameState = runningGameState.removeCharacterFromMap(getCharacterId(0))
        val result =  GameStateValidator()(newGameState).validateBasicMoveCharacter(playerIds(0),
          CoordinateSeq((0, 0), (1, 0)),
          getCharacterId(0)
        )
        assertCommandFailure(result)
      }

      "disallow if character is grounded" in {
        val newGameState = runningGameState.addEffect(getCharacterId(0), GroundEffect(1))

        val result = GameStateValidator()(newGameState).validateBasicMoveCharacter(
          playerIds(0),
          CoordinateSeq((0, 0), (1, 0)),
          getCharacterId(0)
        )
        assertCommandFailure(result)
      }

      "disallow if character is snared" in {
        val newGameState = runningGameState.addEffect(getCharacterId(0), SnareEffect(1))

        val result = GameStateValidator()(newGameState).validateBasicMoveCharacter(
          playerIds(0),
          CoordinateSeq((0, 0), (1, 0)),
          getCharacterId(0)
        )
        assertCommandFailure(result)
      }

      "disallow if character is stunned" in {
        val newGameState = runningGameState.addEffect(getCharacterId(0), StunEffect(1))

        val result = GameStateValidator()(newGameState).validateBasicMoveCharacter(
          playerIds(0),
          CoordinateSeq((0, 0), (1, 0)),
          getCharacterId(0)
        )
        assertCommandFailure(result)
      }

      "disallow empty moves" in {
        val result = validator.validateBasicMoveCharacter(playerIds(0),
          CoordinateSeq(),
          getCharacterId(0)
        )
        assertCommandFailure(result)

        val result2 = validator.validateBasicMoveCharacter(playerIds(0),
          CoordinateSeq((0, 0)),
          getCharacterId(0)
        )
        assertCommandFailure(result2)
      }

      "disallow move from other cell than characters" in {
        val result = validator.validateBasicMoveCharacter(playerIds(0),
          CoordinateSeq((1, 0), (2, 0)),
          getCharacterId(0)
        )
        assertCommandFailure(result)
      }

      "disallow move outside of turn" in {
        val result = validator.validateBasicMoveCharacter(playerIds(1),
          CoordinateSeq((3, 0), (2, 0)),
          getCharacterId(1)
        )
        assertCommandFailure(result)
      }

      "disallow move foreign characters" in {
        val result = validator.validateBasicMoveCharacter(playerIds(0),
          CoordinateSeq((3, 0), (2, 0)),
          getCharacterId(1)
        )
        assertCommandFailure(result)
      }


      "disallow move above speed range" in {
        val result = validator.validateBasicMoveCharacter(playerIds(0),
          CoordinateSeq((0, 0), (1, 0), (2, 0), (2, 1), (2, 2)),
          getCharacterId(0)
        )
        assertCommandFailure(result)
      }

      "disallow move into the same position" in {
        val result = validator.validateBasicMoveCharacter(playerIds(0),
          CoordinateSeq((0, 0), (1, 0), (0, 0)),
          getCharacterId(0)
        )
        assertCommandFailure(result)
      }

      "disallow move that visits another cell more than once" in {
        val result = validator.validateBasicMoveCharacter(playerIds(0),
          CoordinateSeq((0, 0), (1, 0), (2, 0), (1, 0)),
          getCharacterId(0)
        )
        assertCommandFailure(result)
      }

      "disallow move if character cannot move (already moved or made other actions)" in {
        fail()
      }

      "disallow move if there is an obstacle on path" in {
        val result = validator.validateBasicMoveCharacter(playerIds(0),
          CoordinateSeq((0, 0), (1, -1), (1, 0)),
          getCharacterId(0)
        )
        assertCommandFailure(result)
      }

      "disallow move if cell at the end is not free to move" in {
        val result = validator.validateBasicMoveCharacter(playerIds(0),
          CoordinateSeq((0, 0), (1, -1)),
          getCharacterId(0)
        )
        assertCommandFailure(result)
      }

      "disallow move if moving to not adjacent cell" in {
        val result = validator.validateBasicMoveCharacter(playerIds(0),
          CoordinateSeq((0, 0), (0, 2)),
          getCharacterId(0)
        )
        assertCommandFailure(result)
      }
    }

    "validate attacking characters and" when {
      val moveGameState = runningGameState.moveCharacter(CoordinateSeq((0, 0), (1, 0), (2, 0)), getCharacterId(0))

      "allow if character is in attack range" in {
        val result = GameStateValidator()(moveGameState).validateBasicAttackCharacter(playerIds(0),
          getCharacterId(0),
          getCharacterId(1),
        )

        assertCommandSuccess(result)
      }

      "disallow if character is not on the map" in {
        val newGameState = moveGameState.removeCharacterFromMap(getCharacterId(0))

        val result = GameStateValidator()(newGameState).validateBasicAttackCharacter(playerIds(0),
          getCharacterId(0),
          getCharacterId(1),
        )
        assertCommandFailure(result)
      }

      "allow if character is grounded" in {
        val newGameState = moveGameState.addEffect(getCharacterId(0), GroundEffect(1))

        val result = GameStateValidator()(newGameState).validateBasicAttackCharacter(
          playerIds(0),
          getCharacterId(0),
          getCharacterId(1),
        )
        assertCommandSuccess(result)
      }

      "allow if character is snared" in {
        val newGameState = moveGameState.addEffect(getCharacterId(0), SnareEffect(1))

        val result = GameStateValidator()(newGameState).validateBasicAttackCharacter(
          playerIds(0),
          getCharacterId(0),
          getCharacterId(1),
        )
        assertCommandSuccess(result)
      }

      "disallow character is stunned" in {
        val newGameState = moveGameState.addEffect(getCharacterId(0), StunEffect(1))

        val result = GameStateValidator()(newGameState).validateBasicAttackCharacter(
          playerIds(0),
          getCharacterId(0),
          getCharacterId(1),
        )
        assertCommandFailure(result)
      }

      "disallow character is disarmed" in {
        val newGameState = moveGameState.addEffect(getCharacterId(0), DisarmEffect(1))

        val result = GameStateValidator()(newGameState).validateBasicAttackCharacter(
          playerIds(0),
          getCharacterId(0),
          getCharacterId(1),
        )
        assertCommandFailure(result)
      }

      "disallow if character is not in attack range" in {
        val result = validator.validateBasicAttackCharacter(playerIds(0),
          getCharacterId(0),
          getCharacterId(1),
        )

        assertCommandFailure(result)
      }
    }
  }
}
