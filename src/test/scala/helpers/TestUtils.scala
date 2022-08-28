package helpers

import com.tosware.nkm.Logging
import com.tosware.nkm.models.CommandResponse.{CommandResponse, Failure, Success}
import com.tosware.nkm.models.game.NkmCharacter.CharacterId
import com.tosware.nkm.models.game.PickType.BlindPick
import com.tosware.nkm.models.game.Player.PlayerId
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.hex.HexCoordinates
import com.tosware.nkm.providers.HexMapProvider
import com.tosware.nkm.providers.HexMapProvider.TestHexMapName
import org.scalatest.Assertions.fail

import scala.util.Random

trait TestUtils extends Logging {
  implicit val random: Random = new Random()

  protected def assertCommandSuccess(c: CommandResponse): Unit = c match {
    case Success(_) =>
    case Failure(m) =>
      logger.error(m)
      fail()
  }

  protected def assertCommandFailure(c: CommandResponse): Unit = c match {
    case Success(_) => fail()
    case Failure(m) => logger.info(m)
  }

  protected def characterIdOnPoint(hexCoordinates: HexCoordinates)(implicit gameState: GameState): CharacterId =
    gameState.hexMap.get.getCell(hexCoordinates).get.characterId.get

  protected def characterOnPoint(hexCoordinates: HexCoordinates)(implicit gameState: GameState): NkmCharacter =
    gameState.characterById(characterIdOnPoint(hexCoordinates)).get

  protected def getTestGameState(testHexMapName: TestHexMapName, characterMetadatass: Seq[Seq[CharacterMetadata]]): GameState = {
    val playerIds: Seq[PlayerId] = characterMetadatass.indices map(p => s"p$p")
    val hexMap = HexMapProvider().getTestHexMap(testHexMapName)

    logger.info(hexMap.toTextUi)

    val gameStateDeps = GameStartDependencies(
      players = playerIds.map(n => Player(n)),
      hexMap = hexMap,
      pickType = BlindPick,
      numberOfBansPerPlayer = 0,
      numberOfCharactersPerPlayer = characterMetadatass.head.size,
      charactersMetadata = characterMetadatass.flatten.toSet,
      clockConfig = ClockConfig.empty()
    )
    val playersWithMetadatas = (playerIds zip characterMetadatass).toMap

    val startedGameState: GameState = GameState.empty("test").startGame(gameStateDeps)
    val placingGameState: GameState = playersWithMetadatas.foldLeft(startedGameState){
      case (acc, (playerId, characterMetadatas)) => acc.blindPick(playerId, characterMetadatas.map(_.id).toSet)
    }.startPlacingCharacters()

    val playersWithCharacters = placingGameState.players

    val runningGameState = playersWithCharacters.foldLeft(placingGameState){
      case (acc, p) =>
        val spawnPoints = placingGameState.hexMap.get.getSpawnPointsFor(p.id)(placingGameState)
        val spawnsWithCharacters = spawnPoints.map(_.coordinates) zip p.characterIds
        acc.placeCharacters(p.id, spawnsWithCharacters.toMap)
    }
    runningGameState
  }

}

case class Simple2v2TestScenario(metadata: CharacterMetadata, mapName: TestHexMapName = TestHexMapName.Simple2v2) extends TestUtils {
  val gameState: GameState = getTestGameState(mapName, Seq(
    Seq(metadata.copy(name = "Empty1"), metadata.copy(name = "Empty2")),
    Seq(metadata.copy(name = "Empty3"), metadata.copy(name = "Empty4")),
  ))

  object spawnCoordinates {
    val p0First: HexCoordinates = HexCoordinates(0, 0)
    val p0Second: HexCoordinates = HexCoordinates(-1, 0)
    val p1First: HexCoordinates = HexCoordinates(3, 0)
    val p1Second: HexCoordinates = HexCoordinates(4, 0)
  }
  object characters {
    val p0First: NkmCharacter = characterOnPoint(spawnCoordinates.p0First)(gameState)
    val p0Second: NkmCharacter = characterOnPoint(spawnCoordinates.p0Second)(gameState)
    val p1First: NkmCharacter = characterOnPoint(spawnCoordinates.p1First)(gameState)
    val p1Second: NkmCharacter = characterOnPoint(spawnCoordinates.p1Second)(gameState)
  }
}
case class OgreCutterTestScenario(metadata: CharacterMetadata) extends TestUtils {
  val gameState: GameState = getTestGameState(TestHexMapName.OgreCutter, Seq(
    Seq(metadata.copy(name = "Empty1")),
    Seq(metadata.copy(name = "Empty2")),
  ))

  object spawnCoordinates {
    val p0: HexCoordinates = HexCoordinates(0, 0)
    val p1: HexCoordinates = HexCoordinates(3, 0)
  }
  object characters {
    val p0: NkmCharacter = characterOnPoint(spawnCoordinates.p0)(gameState)
    val p1: NkmCharacter = characterOnPoint(spawnCoordinates.p1)(gameState)
  }
}
