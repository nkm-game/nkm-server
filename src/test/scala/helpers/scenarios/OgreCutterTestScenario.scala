package helpers.scenarios

import com.tosware.nkm.models.game.hex.{HexCoordinates, TestHexMapName}
import com.tosware.nkm.models.game.{CharacterMetadata, GameState, NkmCharacter}
import helpers.TestUtils

case class OgreCutterTestScenario(metadata: CharacterMetadata) extends TestUtils {
  val gameState: GameState = getTestGameState(
    TestHexMapName.OgreCutter, Seq(
      Seq(metadata.copy(name = "Empty1")),
      Seq(metadata.copy(name = "Empty2")),
    )
  )

  object spawnCoordinates {
    val p0: HexCoordinates = HexCoordinates(0, 0)
    val p1: HexCoordinates = HexCoordinates(3, 0)
  }

  object characters {
    val p0: NkmCharacter = characterOnPoint(spawnCoordinates.p0)(gameState)
    val p1: NkmCharacter = characterOnPoint(spawnCoordinates.p1)(gameState)
  }
}
