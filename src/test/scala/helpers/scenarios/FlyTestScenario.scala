package helpers.scenarios

import com.tosware.nkm.models.game.GameState
import com.tosware.nkm.models.game.character.CharacterMetadata
import com.tosware.nkm.models.game.hex.TestHexMapName
import helpers.TestScenario

case class FlyTestScenario(metadata: CharacterMetadata)
    extends TestScenario {
  val gameState: GameState = getTestGameStateCustom(
    TestHexMapName.Fly,
    Seq(
      Seq(metadata.copy(name = "Empty1")),
      Seq(metadata.copy(name = "Empty2")),
    ),
  )
}
