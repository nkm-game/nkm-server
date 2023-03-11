package helpers

import com.tosware.nkm.PlayerId
import com.tosware.nkm.models.game.GameState

trait TestScenario extends TestUtils {
  val gameState: GameState
  lazy val p: Seq[Seq[TestCharacterData]] = bindPlayerData()(gameState)
  lazy val owners: Seq[PlayerId] = gameState.players.map(_.id)
}
