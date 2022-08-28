package unit.abilities.hecate

import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.abilities.hecate.{MasterThrone, PowerOfExistence}
import com.tosware.nkm.models.game.hex.HexUtils._
import helpers.{Simple2v2TestScenario, TestUtils}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class PowerOfExistenceSpec
  extends AnyWordSpecLike
    with Matchers
    with TestUtils
{
  private val metadata = CharacterMetadata.empty().copy(initialAbilitiesMetadataIds = Seq(PowerOfExistence.metadata.id, MasterThrone.metadata.id))
  private val s = Simple2v2TestScenario(metadata)
  private implicit val gameState: GameState = s.gameState
  private val abilityId = s.characters.p0First.state.abilities.head.id
  private val masterThroneAbilityId = s.characters.p0First.state.abilities.tail.head.id

  PowerOfExistence.metadata.name must {
    "be able to damage characters" in {
      val aaGameState: GameState = gameState.basicAttack(s.characters.p0First.id, s.characters.p1First.id)
      aaGameState.abilityById(masterThroneAbilityId).get.asInstanceOf[MasterThrone].collectedEnergy should be > 0

      val validator = GameStateValidator()(aaGameState)
      val r = validator.validateAbilityUseWithoutTarget(s.characters.p0First.owner.id, abilityId)
      assertCommandSuccess(r)

      val abilityUsedGameState: GameState = aaGameState.useAbilityWithoutTarget(abilityId)
      abilityUsedGameState.abilityById(masterThroneAbilityId).get.asInstanceOf[MasterThrone].collectedEnergy should be(0)

      abilityUsedGameState.gameLog.events
        .ofType[GameEvent.CharacterDamaged]
        .causedBy(abilityId)
        .size should be (2)
    }
  }
}