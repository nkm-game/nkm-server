package unit.abilities.hecate

import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.abilities.hecate.{MasterThrone, PowerOfExistence}
import com.tosware.nkm.models.game.character.CharacterMetadata
import com.tosware.nkm.models.game.event.GameEvent
import helpers.{TestUtils, scenarios}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class PowerOfExistenceSpec
  extends AnyWordSpecLike
    with Matchers
    with TestUtils
{
  private val abilityMetadata = PowerOfExistence.metadata
  private val metadata = CharacterMetadata.empty().copy(initialAbilitiesMetadataIds = Seq(abilityMetadata.id, MasterThrone.metadata.id))
  private val s = scenarios.Simple2v2TestScenario(metadata)
  private implicit val gameState: GameState = s.gameState.incrementPhase(4)
  private val abilityId = s.p(0)(0).character.state.abilities.head.id
  private val masterThroneAbilityId = s.p(0)(0).character.state.abilities.tail.head.id
  private val aaGameState: GameState =
    gameState
      .basicAttack(s.p(0)(0).character.id, s.p(1)(0).character.id)
      .endTurn()
      .passTurn(s.p(1)(0).character.id)
      .finishPhase()

  abilityMetadata.name must {
    "be able to use" in {
      assertCommandSuccess {
        GameStateValidator()(aaGameState)
          .validateAbilityUse(s.p(0)(0).character.owner.id, abilityId)
      }
    }

    "be able to damage characters" in {
      aaGameState.abilityById(masterThroneAbilityId).asInstanceOf[MasterThrone].collectedEnergy(aaGameState) should be > 0
      val abilityUsedGameState: GameState = aaGameState.useAbility(abilityId)
      abilityUsedGameState.abilityById(masterThroneAbilityId).asInstanceOf[MasterThrone].collectedEnergy(abilityUsedGameState) should be(0)

      abilityUsedGameState.gameLog.events
        .ofType[GameEvent.CharacterDamaged]
        .causedBy(abilityId)
        .size should be (2)
    }
  }
}