package helpers

import com.tosware.nkm.models.CommandResponse._
import com.tosware.nkm.models.game.NkmCharacter.CharacterId
import com.tosware.nkm.models.game.PickType.BlindPick
import com.tosware.nkm.models.game.Player.PlayerId
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.hex.{HexCoordinates, TestHexMapName}
import com.tosware.nkm.providers.HexMapProvider
import com.tosware.nkm.{Logging, NkmUtils}
import org.scalatest.Assertions.fail

import scala.util.Random

trait TestUtils extends Logging with NkmUtils {
  implicit val random: Random = new Random()
  implicit val causedById: String = "test"

  protected def assertCommandSuccess(c: CommandResponse): Unit = c match {
    case Success(_) =>
    case Failure(m) =>
      logger.error(m)
      fail(m)
  }

  protected def assertCommandFailure(c: CommandResponse): Unit = c match {
    case Success(_) => fail()
    case Failure(m) => logger.info(m)
  }

  protected def characterIdOnPoint(hexCoordinates: HexCoordinates)(implicit gameState: GameState): CharacterId =
    gameState.hexMap.getCell(hexCoordinates).get.characterId.get

  protected def characterOnPoint(hexCoordinates: HexCoordinates)(implicit gameState: GameState): NkmCharacter =
    gameState.characterById(characterIdOnPoint(hexCoordinates))

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
        val spawnPoints = placingGameState.hexMap.getSpawnPointsFor(p.id)(placingGameState)
        val spawnsWithCharacters = spawnPoints.map(_.coordinates) zip p.characterIds
        acc.placeCharacters(p.id, spawnsWithCharacters.toMap)
    }
    runningGameState
  }
}
