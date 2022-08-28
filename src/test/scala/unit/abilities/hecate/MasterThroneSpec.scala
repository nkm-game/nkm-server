package unit.abilities.hecate

import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.abilities.hecate.{Aster, MasterThrone}
import helpers.{Simple2v2TestScenario, TestUtils}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class MasterThroneSpec
  extends AnyWordSpecLike
    with Matchers
    with TestUtils
{
  private val metadata = CharacterMetadata.empty().copy(initialAbilitiesMetadataIds = Seq(MasterThrone.metadata.id, Aster.metadata.id))
  private val s = Simple2v2TestScenario(metadata)
  private implicit val gameState: GameState = s.gameState
  private val abilityId = s.characters.p0First.state.abilities.head.id
  private val asterAbilityId = s.characters.p0First.state.abilities.tail.head.id

  MasterThrone.metadata.name must {
    "not be initialized with energy" in {
      gameState.abilityById(abilityId).get.asInstanceOf[MasterThrone].collectedEnergy should be(0)
    }

    "be able to collect energy from basic attacks" in {
      val newGameState: GameState = gameState.basicAttack(s.characters.p0First.id, s.characters.p1First.id)
      newGameState.abilityById(abilityId).get.asInstanceOf[MasterThrone].collectedEnergy should be > 0
    }

    "be able to collect energy from normal ability" in {
      val newGameState: GameState = gameState.useAbilityOnCoordinates(asterAbilityId, s.spawnCoordinates.p0Second)
      newGameState.abilityById(abilityId).get.asInstanceOf[MasterThrone].collectedEnergy should be > 0
    }
  }
}