package helpers

import com.tosware.nkm._
import com.tosware.nkm.models.CommandResponse._
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.character.{CharacterMetadata, NkmCharacter, StatType}
import com.tosware.nkm.models.game.character_effect.CharacterEffectName
import com.tosware.nkm.models.game.hex.{HexCoordinates, TestHexMapName}
import com.tosware.nkm.models.game.pick.PickType.BlindPick
import com.tosware.nkm.providers.HexMapProvider
import com.tosware.nkm.serializers.NkmJsonProtocol
import org.scalatest.Assertion
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import scala.annotation.tailrec
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
      val spawnCoords = gameState
        .hexMap
        .getSpawnPointsFor(pid)(gameState)
        .map(_.coordinates)
        .toSeq
        .sortBy(_.toTuple)
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

  protected def assertEffectsExist(effectNames: Seq[CharacterEffectName], cid: CharacterId)(gameState: GameState): Assertion =
    gameState
      .characterById(cid)
      .state
      .effects
      .map(_.metadata.name) should contain allElementsOf effectNames

  protected def assertEffectsDoNotExist(effectNames: Seq[CharacterEffectName], cid: CharacterId)(gameState: GameState): Assertion =
    gameState
      .characterById(cid)
      .state
      .effects
      .map(_.metadata.name) should contain noElementsOf effectNames

  protected def assertEffectDoesNotExistsOfType[A: ClassTag](cid: CharacterId)(gameState: GameState): Assertion =
    gameState
      .characterById(cid)
      .state
      .effects
      .ofType[A]
      .size should be (0)

  protected def assertBuffExists(statType: StatType, cid: CharacterId)(gameState: GameState): Assertion =
    gameState
      .characterById(cid)
      .state
      .effects
      .ofType[effects.StatBuff]
      .map(_.statType) should contain (statType)

  protected def assertBuffDoesNotExist(statType: StatType, cid: CharacterId)(gameState: GameState): Assertion =
    gameState
      .characterById(cid)
      .state
      .effects
      .ofType[effects.StatBuff]
      .map(_.statType) should not contain (statType)

  protected def characterIdOnPoint(hexCoordinates: HexCoordinates)(implicit gameState: GameState): CharacterId =
    gameState.hexMap.getCell(hexCoordinates).get.characterId.get

  protected def characterOnPoint(hexCoordinates: HexCoordinates)(implicit gameState: GameState): NkmCharacter =
    gameState.characterById(characterIdOnPoint(hexCoordinates))

  protected def getTestGameState(testHexMapName: TestHexMapName, metadata: CharacterMetadata): GameState = {
    val hexMap = HexMapProvider().getTestHexMap(testHexMapName)
    val characterMetadatass = hexMap.numberOfSpawnsPerPlayer
      .map { case (playerIndex, numberOfSpawns) =>
        (0 until numberOfSpawns).map(x => metadata.copy(name = s"p($playerIndex)($x)"))
      }.toSeq

    getTestGameStateCustom(testHexMapName, characterMetadatass)
  }

  protected def getTestGameStateCustom(testHexMapName: TestHexMapName, characterMetadatass: Seq[Seq[CharacterMetadata]]): GameState = {
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

  private def _passAllCharactersInNPhases(gs: GameState, n: Int): GameState =
    Function.chain(List.fill(n)(_passAllCharactersInCurrentPhase))(gs)

  private def _passAllCharactersInCurrentPhase(gs: GameState): GameState =
    _passAllCharactersInPhase(gs, gs.phase.number)


  @tailrec
  private final def _passAllCharactersInPhase(gs: GameState, phaseNumber: Int): GameState =
  {
    val ngs = gs.characterTakingActionThisTurn.fold(gs)(_ => gs.endTurn())
    if(ngs.phase.number != phaseNumber) return ngs

    val charactersToPass = ngs.currentPlayer.characterIds.intersect(ngs.charactersToTakeAction)
    _passAllCharactersInPhase(ngs.passTurn(charactersToPass.head), phaseNumber)
  }

  implicit class GameStateUtils(gs: GameState) {
    def passAllCharactersInNPhases(n: Int): GameState =
      _passAllCharactersInNPhases(gs, n)

    def passAllCharactersInCurrentPhase(): GameState =
      _passAllCharactersInCurrentPhase(gs)

    final def passAllCharactersInPhase(phaseNumber: Int): GameState =
      _passAllCharactersInPhase(gs, phaseNumber)
  }
}
