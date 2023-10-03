package helpers.scenarios

import com.tosware.nkm.models.game.GameState
import com.tosware.nkm.models.game.character.CharacterMetadata
import com.tosware.nkm.models.game.hex.TestHexMapName
import helpers.TestScenario

case class Simple1v9LineTestScenario(metadata: CharacterMetadata)
    extends TestScenario {
  val gameState: GameState = getTestGameStateCustom(
    TestHexMapName.Simple1v9Line,
    Seq(
      Seq(metadata.copy(name = "Character")),
      (1 to 9).map(n => CharacterMetadata.empty(s"Enemy ($n)")),
    ),
  )
}
