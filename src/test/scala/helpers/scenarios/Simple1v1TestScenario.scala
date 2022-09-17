package helpers.scenarios

import com.tosware.nkm.models.game.hex.HexCoordinates
import com.tosware.nkm.models.game.{CharacterMetadata, GameState, NkmCharacter}
import com.tosware.nkm.providers.HexMapProvider.TestHexMapName
import helpers.TestUtils

case class Simple1v1TestScenario(metadata: CharacterMetadata) extends TestUtils {
  val gameState: GameState = getTestGameState(
    TestHexMapName.Simple1v1, Seq(
      Seq(metadata.copy(name = "Character")),
      Seq(metadata.copy(name = "Enemy")),
    )
  )

  object spawnCoordinates {
    val p0: HexCoordinates = HexCoordinates(0, 0)
    val p1: HexCoordinates = HexCoordinates(1, 0)
  }

  object characters {
    val p0: NkmCharacter = characterOnPoint(spawnCoordinates.p0)(gameState)
    val p1: NkmCharacter = characterOnPoint(spawnCoordinates.p1)(gameState)
  }
}
