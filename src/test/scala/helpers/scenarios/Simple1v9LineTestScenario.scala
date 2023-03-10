package helpers.scenarios

import com.tosware.nkm.models.game.GameState
import com.tosware.nkm.models.game.character.{CharacterMetadata, NkmCharacter}
import com.tosware.nkm.models.game.hex.{HexCoordinates, TestHexMapName}
import helpers.TestUtils

case class Simple1v9LineTestScenario(metadata: CharacterMetadata) extends TestUtils {
  val gameState: GameState = getTestGameState(
    TestHexMapName.Simple1v9Line, Seq(
      Seq(metadata.copy(name = "Character")),
      (1 to 9).map(n => CharacterMetadata.empty(s"Enemy ($n)")),
    )
  )

  object spawnCoordinates {
    val p0: HexCoordinates = HexCoordinates(0, 0)
    val p1: Seq[HexCoordinates] = (1 to 9).map(n => HexCoordinates(n, 0))
  }

  object characters {
    val p0: NkmCharacter = characterOnPoint(spawnCoordinates.p0)(gameState)
    val p1: Seq[NkmCharacter] = spawnCoordinates.p1.map(c => characterOnPoint(c)(gameState))
  }
}
