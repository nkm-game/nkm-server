package helpers.scenarios

import com.tosware.nkm.models.game.hex.{HexCoordinates, TestHexMapName}
import com.tosware.nkm.models.game.{CharacterMetadata, GameState, NkmCharacter}
import helpers.TestUtils

case class Simple2v2TestScenario(metadata: CharacterMetadata, mapName: TestHexMapName = TestHexMapName.Simple2v2) extends TestUtils {
  val gameState: GameState = getTestGameState(
    mapName, Seq(
      Seq(metadata.copy(name = "Empty1"), metadata.copy(name = "Empty2")),
      Seq(metadata.copy(name = "Empty3"), metadata.copy(name = "Empty4")),
    )
  )

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
