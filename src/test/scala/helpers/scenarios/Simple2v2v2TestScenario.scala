package helpers.scenarios

import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.character.{CharacterMetadata, NkmCharacter}
import com.tosware.nkm.models.game.hex.{HexCoordinates, TestHexMapName}
import helpers.TestScenario

case class Simple2v2v2TestScenario(metadata: CharacterMetadata)
  extends TestScenario
{
  val gameState: GameState = getTestGameState(
    TestHexMapName.Simple2v2v2, Seq(
      Seq(metadata.copy(name = "Empty1"), metadata.copy(name = "Empty2")),
      Seq(metadata.copy(name = "Empty3"), metadata.copy(name = "Empty4")),
      Seq(metadata.copy(name = "Empty5"), metadata.copy(name = "Empty6")),
    )
  )

  object spawnCoordinates {
    val p0First: HexCoordinates = HexCoordinates(0, 0)
    val p0Second: HexCoordinates = HexCoordinates(-1, 0)
    val p1First: HexCoordinates = HexCoordinates(3, 0)
    val p1Second: HexCoordinates = HexCoordinates(4, 0)
    val p2First: HexCoordinates = HexCoordinates(1, 1)
    val p2Second: HexCoordinates = HexCoordinates(2, 1)
  }

  object characters {
    val p0First: NkmCharacter = characterOnPoint(spawnCoordinates.p0First)(gameState)
    val p0Second: NkmCharacter = characterOnPoint(spawnCoordinates.p0Second)(gameState)
    val p1First: NkmCharacter = characterOnPoint(spawnCoordinates.p1First)(gameState)
    val p1Second: NkmCharacter = characterOnPoint(spawnCoordinates.p1Second)(gameState)
    val p2First: NkmCharacter = characterOnPoint(spawnCoordinates.p2First)(gameState)
    val p2Second: NkmCharacter = characterOnPoint(spawnCoordinates.p2Second)(gameState)
  }
}
