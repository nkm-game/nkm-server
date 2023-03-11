package helpers

import com.tosware.nkm._
import com.tosware.nkm.models.CommandResponse._
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.character.{CharacterMetadata, NkmCharacter, StatType}
import com.tosware.nkm.models.game.hex.{HexCoordinates, TestHexMapName}
import com.tosware.nkm.models.game.pick.PickType.BlindPick
import com.tosware.nkm.providers.HexMapProvider
import com.tosware.nkm.serializers.NkmJsonProtocol
import org.scalatest.Assertion
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import scala.reflect.ClassTag
import scala.util.Random

trait TestUtils
  extends AnyWordSpecLike
    with Matchers
    with Logging
    with NkmJsonProtocol
    {
  implicit val random: Random = new Random()
  implicit val causedById: String = "test"

  case class TestCharacterData(spawnCoordinates: HexCoordinates)(implicit gameState: GameState) {
    def character: NkmCharacter = characterOnPoint(spawnCoordinates)(gameState)
    def ownerId: PlayerId = character.owner(gameState).id
  }

  protected def bindPlayerData()(implicit gameState: GameState): Seq[Seq[TestCharacterData]] =
    gameState.players.map(_.id).map(pid => {
      val spawnCoords = gameState.hexMap.getSpawnPointsFor(pid)(gameState).map(_.coordinates).toSeq
      bindCharacterData(spawnCoords)(gameState)
    })

  protected def bindCharacterData(cs: Seq[HexCoordinates])(implicit gameState: GameState): Seq[TestCharacterData] =
    cs.map(c => TestCharacterData(c))

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

  protected def assertEffectExistsOfType[A: ClassTag](cid: CharacterId)(gameState: GameState): Assertion =
    gameState
      .characterById(cid)
      .state
      .effects
      .ofType[A]
      .size should be > 0

  protected def assertBuffExists(statType: StatType, cid: CharacterId)(gameState: GameState): Assertion =
    gameState
      .characterById(cid)
      .state
      .effects
      .ofType[effects.StatBuff]
      .map(_.statType) should contain (statType)

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
