package unit.abilities.hecate

import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.abilities.hecate.*
import com.tosware.nkm.models.game.character.CharacterMetadata
import helpers.{TestUtils, scenarios}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class MasterThroneSpec
  extends AnyWordSpecLike
    with Matchers
    with TestUtils
{
  private val abilityMetadata = MasterThrone.metadata
  private val metadata = CharacterMetadata.empty()
    .copy(initialAbilitiesMetadataIds = Seq(
      abilityMetadata.id,
      Aster.metadata.id,
      PowerOfExistence.metadata.id,
    ))
  private val s = scenarios.Simple2v2TestScenario(metadata)
  private val asterAbilityId =
    s.defaultCharacter.state.abilities(1).id
  private val powerOfExistenceAbilityId =
    s.defaultCharacter.state.abilities(2).id

  private def collectedEnergy(gs: GameState): Int =
    gs.abilityById(s.defaultAbilityId)
      .asInstanceOf[MasterThrone]
      .collectedEnergy(gs)

  private val aaGs: GameState = s.gameState.basicAttack(s.p(0)(0).character.id, s.p(1)(0).character.id)
  private val asterGs: GameState = s.gameState.useAbilityOnCoordinates(asterAbilityId, s.p(0)(1).spawnCoordinates)
  private val poeGs: GameState = s.gameState.useAbility(powerOfExistenceAbilityId)

  abilityMetadata.name must {
    "not be initialized with energy" in {
      collectedEnergy(s.gameState) should be(0)
    }

    "be able to collect energy from basic attacks" in {
      collectedEnergy(aaGs) should be > 0
    }

    "be able to collect energy from normal ability" in {
      collectedEnergy(asterGs) should be > 0
    }

    "not be able to collect energy from ultimate ability" in {
      collectedEnergy(poeGs) should be (0)
    }
  }
}