package helpers.scenarios

import com.tosware.nkm.models.game.GameState
import com.tosware.nkm.models.game.character.CharacterMetadata
import com.tosware.nkm.models.game.hex.TestHexMapName
import helpers.TestScenario

case class Spacey1v1TestScenario(metadata: CharacterMetadata, secondMetadata: Option[CharacterMetadata] = None)
    extends TestScenario {
  private val metadata2 = secondMetadata.getOrElse(metadata)
  val gameState: GameState = getTestGameStateCustom(
    TestHexMapName.Simple1v1,
    Seq(
      Seq(metadata.copy(name = "Character")),
      Seq(metadata2.copy(name = "Enemy")),
    ),
  )
}
