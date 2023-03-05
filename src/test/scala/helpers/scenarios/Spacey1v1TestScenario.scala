package helpers.scenarios

import com.tosware.nkm.models.game.GameState
import com.tosware.nkm.models.game.Player.PlayerId
import com.tosware.nkm.models.game.character.{CharacterMetadata, NkmCharacter}
import com.tosware.nkm.models.game.hex.{HexCoordinates, TestHexMapName}
import helpers.TestUtils

case class Spacey1v1TestScenario(metadata: CharacterMetadata, secondMetadata: Option[CharacterMetadata] = None) extends TestUtils {
  private val metadata2 = secondMetadata.getOrElse(metadata)
  val gameState: GameState = getTestGameState(
    TestHexMapName.Simple1v1, Seq(
      Seq(metadata.copy(name = "Character")),
      Seq(metadata2.copy(name = "Enemy")),
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

  object owners {
    val p0: PlayerId = characters.p0.owner(gameState).id
    val p1: PlayerId = characters.p1.owner(gameState).id
  }
}
