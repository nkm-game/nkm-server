package unit.abilities.kazuma

import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.abilities.satou_kazuma.Steal
import com.tosware.nkm.models.game.ability.UseData
import com.tosware.nkm.models.game.hex.TestHexMapName
import helpers.{TestScenario, TestUtils}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class StealSpec
    extends AnyWordSpecLike
    with Matchers
    with TestUtils {
  private val abilityMetadata = Steal.metadata
  private val s = TestScenario.generate(TestHexMapName.Simple1v1, abilityMetadata.id)
  private val aGs: GameState =
    s.ultGs.useAbility(s.defaultAbilityId, UseData(s.defaultEnemy.id))

  private def assertStealActive(gs: GameState) = {
    val newMagicalDefense = s.defaultCharacter.state.pureMagicalDefense + s.defaultEnemy.state.pureMagicalDefense
    val newPhysicalDefense =
      s.defaultCharacter.state.purePhysicalDefense + s.defaultEnemy.state.purePhysicalDefense

    gs.characterById(s.defaultCharacter.id).state.pureMagicalDefense should be(newMagicalDefense)
    gs.characterById(s.defaultCharacter.id).state.purePhysicalDefense should be(newPhysicalDefense)

    gs.characterById(s.defaultEnemy.id).state.pureMagicalDefense should be(0)
    gs.characterById(s.defaultEnemy.id).state.purePhysicalDefense should be(0)
  }

  private def assertStealInactive(gs: GameState) = {
    gs.characterById(s.defaultCharacter.id).state.pureMagicalDefense should be(
      s.defaultCharacter.state.pureMagicalDefense
    )
    gs.characterById(s.defaultCharacter.id).state.purePhysicalDefense should be(
      s.defaultCharacter.state.purePhysicalDefense
    )

    gs.characterById(s.defaultEnemy.id).state.pureMagicalDefense should be(
      s.defaultEnemy.state.pureMagicalDefense
    )
    gs.characterById(s.defaultEnemy.id).state.purePhysicalDefense should be(
      s.defaultEnemy.state.purePhysicalDefense
    )
  }

  abilityMetadata.name must {
    "be able to use" in {
      assertCommandSuccess {
        GameStateValidator()(s.ultGs)
          .validateAbilityUse(s.owners(0), s.defaultAbilityId, UseData(s.defaultEnemy.id))
      }
    }

    "be able to steal armor" in {
      assertStealActive(aGs)
    }

    "restore armor after duration time" in {
      val duration = abilityMetadata.variables("duration")

      (0 until duration).map { n =>
        logger.info(s"Checking phase $n")
        aGs.passAllCharactersInNPhases(n)
      }.foreach(assertStealActive)

      (duration to duration + 5).map { n =>
        logger.info(s"Checking phase $n")
        aGs.passAllCharactersInNPhases(n)
      }.foreach(assertStealInactive)
    }
  }
}
