package unit.abilities.hecate

import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.abilities.hecate.{MasterThrone, PowerOfExistence}
import com.tosware.nkm.models.game.character.CharacterMetadata
import com.tosware.nkm.models.game.event.GameEvent
import com.tosware.nkm.models.game.hex.TestHexMapName
import helpers.{TestScenario, TestUtils}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class PowerOfExistenceSpec
    extends AnyWordSpecLike
    with Matchers
    with TestUtils {
  private val abilityMetadata = PowerOfExistence.metadata
  private val metadata =
    CharacterMetadata.empty().copy(initialAbilitiesMetadataIds = Seq(abilityMetadata.id, MasterThrone.metadata.id))
  private val s = TestScenario.generate(TestHexMapName.Simple2v2, metadata)
  private val masterThroneAbilityId = s.defaultCharacter.state.abilities.tail.head.id
  private val aaGameState: GameState =
    s.ultGs
      .basicAttack(s.defaultCharacter.id, s.defaultEnemy.id)
      .passAllCharactersInCurrentPhase()

  private val aGs: GameState = aaGameState.useAbility(s.defaultAbilityId)

  private def collectedEnergy(gs: GameState): Int =
    gs.abilityById(masterThroneAbilityId)
      .asInstanceOf[MasterThrone]
      .collectedEnergy(gs)

  abilityMetadata.name must {
    "be able to use" in {
      assertCommandSuccess {
        GameStateValidator()(aaGameState)
          .validateAbilityUse(s.owners(0), s.defaultAbilityId)
      }
    }

    "be able to damage characters" in {
      collectedEnergy(aaGameState) should be > 0
      collectedEnergy(aGs) should be(0)
      aGs.gameLog.events
        .ofType[GameEvent.CharacterDamaged]
        .causedBy(s.defaultAbilityId)
        .size should be(2)
    }
  }
}
