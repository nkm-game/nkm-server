package helpers.scenarios

import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.character.CharacterMetadata
import com.tosware.nkm.models.game.hex.TestHexMapName
import helpers.TestScenario

case class Simple2v2v2TestScenario(metadata: CharacterMetadata)
    extends TestScenario {
  val gameState: GameState = getTestGameStateCustom(
    TestHexMapName.Simple2v2v2,
    Seq(
      Seq(metadata.copy(name = "Empty1"), metadata.copy(name = "Empty2")),
      Seq(metadata.copy(name = "Empty3"), metadata.copy(name = "Empty4")),
      Seq(metadata.copy(name = "Empty5"), metadata.copy(name = "Empty6")),
    ),
  )
}
