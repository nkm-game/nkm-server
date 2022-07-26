package unit.validators

import com.tosware.NKM.models.CommandResponse._
import com.tosware.NKM.models.GameStateValidator
import com.tosware.NKM.models.game.HexCellType.{SpawnPoint, Wall}
import com.tosware.NKM.models.game.PickType.BlindPick
import com.tosware.NKM.models.game._
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
  private val playerIds = Seq("p0", "p1")
  private val gameStateDeps = GameStartDependencies(
    players = playerIds.map(n => Player(n)),
    hexMap = hexMap,
    pickType = BlindPick,
    numberOfBansPerPlayer = 0,
    numberOfCharactersPerPlayer = 1,
    charactersMetadata = Set(NKMCharacterMetadata.empty().copy(initialSpeed = 3)),
    clockConfig = ClockConfig.empty()
  )

  private val placingGameState = GameState.empty("test")
    .startGame(gameStateDeps)
    .blindPick(playerIds(0), Set(gameStateDeps.charactersMetadata.head.id))
    .blindPick(playerIds(1), Set(gameStateDeps.charactersMetadata.head.id))
    .startPlacingCharacters()

  private def getCharacterId(playerId: Int) = placingGameState.players(playerId).characters.head.id

  private val runningGameState = placingGameState
    .placeCharacters(playerIds(0), Map(hexMap.getSpawnPointsByNumber(0).head.coordinates -> getCharacterId(0)))
    .placeCharacters(playerIds(1), Map(hexMap.getSpawnPointsByNumber(1).head.coordinates -> getCharacterId(1)))

  private val validator = GameStateValidator()(runningGameState)

  private def toCoordinateSeq(tuples: Seq[(Int, Int)]) = tuples.map{case (x, z) => HexCoordinates(x, z)}

  "GameStateValidator" must {
    logger.info(hexMap.toTextUi)
    "validate moving characters and" when {
      "allow move within speed range" in {
        validator.validateMoveCharacter(playerIds(0),
          toCoordinateSeq(Seq((0, 0), (1, 0))),
          getCharacterId(0)
        ) match {
          case Success(_) =>
          case Failure(m) => {
            logger.error(m)
            fail()
          }
        }
      }

      "disallow if character is not on the map" in {
        fail()
      }

      "disallow if character is grounded" in {
        fail()
      }

      "disallow if character is snared" in {
        fail()
      }

      "disallow character is stunned" in {
        fail()
      }

      "disallow empty moves" in {
        validator.validateMoveCharacter(playerIds(0),
          toCoordinateSeq(Seq()),
          getCharacterId(0)
        ) match {
          case Success(_) => fail()
          case Failure(m) => logger.info(m)
        }

        validator.validateMoveCharacter(playerIds(0),
          toCoordinateSeq(Seq((0, 0))),
          getCharacterId(0)
        ) match {
          case Success(_) => fail()
          case Failure(m) => logger.info(m)
        }
      }

      "disallow move from other cell than characters" in {
        validator.validateMoveCharacter(playerIds(0),
          toCoordinateSeq(Seq((1, 0), (2, 0))),
          getCharacterId(0)
        ) match {
          case Success(_) => fail()
          case Failure(m) => logger.info(m)
        }
      }

      "disallow move outside of turn" in {
        validator.validateMoveCharacter(playerIds(1),
          toCoordinateSeq(Seq((3, 0), (2, 0))),
          getCharacterId(1)
        ) match {
          case Success(_) => fail()
          case Failure(m) => logger.info(m)
        }
      }

      "disallow move foreign characters" in {
        validator.validateMoveCharacter(playerIds(0),
          toCoordinateSeq(Seq((3, 0), (2, 0))),
          getCharacterId(1)
        ) match {
          case Success(_) => fail()
          case Failure(m) => logger.info(m)
        }
      }


      "disallow move above speed range" in {
        validator.validateMoveCharacter(playerIds(0),
          toCoordinateSeq(Seq((0, 0), (1, 0), (2, 0), (2, 1), (2, 2))),
          getCharacterId(0)
        ) match {
          case Success(_) => fail()
          case Failure(m) => logger.info(m)
        }
      }

      "disallow move into the same position" in {
        validator.validateMoveCharacter(playerIds(0),
          toCoordinateSeq(Seq((0, 0), (1, 0), (0, 0))),
          getCharacterId(0)
        ) match {
          case Success(_) => fail()
          case Failure(m) => logger.info(m)
        }
      }

      "disallow move that visits another cell more than once" in {
        validator.validateMoveCharacter(playerIds(0),
          toCoordinateSeq(Seq((0, 0), (1, 0), (2, 0), (1, 0))),
          getCharacterId(0)
        ) match {
          case Success(_) => fail()
          case Failure(m) => logger.info(m)
        }
      }

      "disallow move if character cannot move (already moved or made other actions)" in {
        fail()
      }

      "disallow move if there is an obstacle on path" in {
        validator.validateMoveCharacter(playerIds(0),
          toCoordinateSeq(Seq((0, 0), (1, -1), (1, 0))),
          getCharacterId(0)
        ) match {
          case Success(_) => fail()
          case Failure(m) => logger.info(m)
        }
      }

      "disallow move if cell at the end is not free to move" in {
        validator.validateMoveCharacter(playerIds(0),
          toCoordinateSeq(Seq((0, 0), (1, -1))),
          getCharacterId(0)
        ) match {
          case Success(_) => fail()
          case Failure(m) => logger.info(m)
        }
      }

      "disallow move if moving to not adjacent cell" in {
        validator.validateMoveCharacter(playerIds(0),
          toCoordinateSeq(Seq((0, 0), (0, 2))),
          getCharacterId(0)
        ) match {
          case Success(_) => fail()
          case Failure(m) => logger.info(m)
        }
      }
    }
  }
}
