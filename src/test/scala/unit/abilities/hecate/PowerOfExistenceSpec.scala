package unit.abilities.hecate

import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.abilities.hecate.{MasterThrone, PowerOfExistence}
import com.tosware.nkm.NkmUtils._
import helpers.{TestUtils, scenarios}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class PowerOfExistenceSpec
  extends AnyWordSpecLike
    with Matchers
    with TestUtils
{
  private val metadata = CharacterMetadata.empty().copy(initialAbilitiesMetadataIds = Seq(PowerOfExistence.metadata.id, MasterThrone.metadata.id))
  private val s = scenarios.Simple2v2TestScenario(metadata)
  private implicit val gameState: GameState = s.gameState.incrementPhase(4)
  private val abilityId = s.characters.p0First.state.abilities.head.id
  private val masterThroneAbilityId = s.characters.p0First.state.abilities.tail.head.id
  private val aaGameState: GameState = gameState.basicAttack(s.characters.p0First.id, s.characters.p1First.id)
    .endTurn()
    .passTurn(s.characters.p1First.id)
    .finishPhase()

  PowerOfExistence.metadata.name must {
    "be able to use" in {
      val validator = GameStateValidator()(aaGameState)
      val r = validator.validateAbilityUseWithoutTarget(s.characters.p0First.owner.id, abilityId)
      assertCommandSuccess(r)
    }

    "be able to damage characters" in {
      aaGameState.abilityById(masterThroneAbilityId).get.asInstanceOf[MasterThrone].collectedEnergy(aaGameState) should be > 0
      val abilityUsedGameState: GameState = aaGameState.useAbilityWithoutTarget(abilityId)
      abilityUsedGameState.abilityById(masterThroneAbilityId).get.asInstanceOf[MasterThrone].collectedEnergy(abilityUsedGameState) should be(0)

      abilityUsedGameState.gameLog.events
        .ofType[GameEvent.CharacterDamaged]
        .causedBy(abilityId)
        .size should be (2)
    }
  }
}