package helpers.scenarios

import com.tosware.nkm.models.game.GameState
import com.tosware.nkm.models.game.character.CharacterMetadata
import com.tosware.nkm.models.game.hex.TestHexMapName
import helpers.TestScenario

case class Simple2v2TestScenario(metadata: CharacterMetadata, mapName: TestHexMapName = TestHexMapName.Simple2v2)
    extends TestScenario {
  val gameState: GameState = getTestGameStateCustom(
    mapName,
    Seq(
      Seq(metadata.copy(name = "Empty1"), metadata.copy(name = "Empty2")),
      Seq(metadata.copy(name = "Empty3"), metadata.copy(name = "Empty4")),
    ),
  )
}
