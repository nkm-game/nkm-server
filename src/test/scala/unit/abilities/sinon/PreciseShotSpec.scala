package unit.abilities.sinon

import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.abilities.sinon.PreciseShot
import helpers.{TestUtils, scenarios}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class PreciseShotSpec
  extends AnyWordSpecLike
    with Matchers
    with TestUtils
{
  private val metadata = CharacterMetadata.empty().copy(initialAbilitiesMetadataIds = Seq(PreciseShot.metadata.id))
  private val s = scenarios.Simple2v2TestScenario(metadata)
  private implicit val gameState: GameState = s.gameState.incrementPhase(4)
  private val abilityId = s.characters.p0First.state.abilities.head.id

  PreciseShot.metadata.name must {
    "be able to use" in {
      val r = GameStateValidator()
        .validateAbilityUseOnCharacter(s.characters.p0First.owner.id, abilityId, s.characters.p1First.id)
      assertCommandSuccess(r)
    }

    "be able to deal damage" in {
      val newGameState: GameState = gameState.useAbilityOnCharacter(abilityId, s.characters.p1First.id)
      newGameState.gameLog.events.ofType[GameEvent.CharacterDamaged].exists(_.causedById == abilityId)
    }
  }
}