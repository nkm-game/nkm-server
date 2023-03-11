package helpers.scenarios

import com.tosware.nkm.models.game.GameState
import com.tosware.nkm.models.game.character.{CharacterMetadata, NkmCharacter}
import com.tosware.nkm.models.game.hex.{HexCoordinates, TestHexMapName}
import helpers.TestScenario

case class SummerBreezeTestScenario(metadata: CharacterMetadata)
  extends TestScenario
{
  val gameState: GameState = getTestGameState(
    TestHexMapName.SummerBreeze, Seq(
      Seq(metadata.copy(name = "Empty1")),
      Seq(metadata.copy(name = "Empty2"), metadata.copy(name = "Empty3")),
    )
  )

  object spawnCoordinates {
    val p0: HexCoordinates = HexCoordinates(0, 0)
    val p1First: HexCoordinates = HexCoordinates(3, 0)
    val p1Second: HexCoordinates = HexCoordinates(4, 0)
  }

  object characters {
    val p0: NkmCharacter = characterOnPoint(spawnCoordinates.p0)(gameState)
    val p1First: NkmCharacter = characterOnPoint(spawnCoordinates.p1First)(gameState)
    val p1Second: NkmCharacter = characterOnPoint(spawnCoordinates.p1Second)(gameState)
  }
}
