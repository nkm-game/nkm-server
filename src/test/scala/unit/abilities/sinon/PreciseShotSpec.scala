package unit.abilities.sinon

import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.abilities.sinon.PreciseShot
import com.tosware.nkm.models.game.hex.HexUtils._
import helpers.{Simple2v2TestScenario, TestUtils}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class PreciseShotSpec
  extends AnyWordSpecLike
    with Matchers
    with TestUtils
{
  private val metadata = CharacterMetadata.empty().copy(initialAbilitiesMetadataIds = Seq(PreciseShot.metadata.id))
  private val s = Simple2v2TestScenario(metadata)
  private implicit val gameState: GameState = s.gameState
  private val abilityId = s.characters.p0First.state.abilities.head.id

  PreciseShot.metadata.name must {
    "be able to deal damage" in {
      val r = GameStateValidator()
        .validateAbilityUseOnCharacter(s.characters.p0First.owner.id, abilityId, s.characters.p1First.id)
      assertCommandSuccess(r)

      val newGameState: GameState = gameState.useAbilityOnCharacter(abilityId, s.characters.p1First.id)
      newGameState.gameLog.events.ofType[GameEvent.CharacterDamaged].exists(_.causedById == abilityId)
    }
  }
}