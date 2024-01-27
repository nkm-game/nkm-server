package unit.abilities.kazuma

import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game.abilities.satou_kazuma.Steal
import com.tosware.nkm.models.game.ability.UseData
import com.tosware.nkm.models.game.character.CharacterMetadata
import com.tosware.nkm.models.game.game_state.GameState
import com.tosware.nkm.models.game.hex.TestHexMapName
import helpers.{TestScenario, TestUtils}

class StealSpec extends TestUtils {
  private val abilityMetadata = Steal.metadata

  private val characterMetadata = CharacterMetadata.empty()
    .copy(
      initialAbilitiesMetadataIds = Seq(abilityMetadata.id),
      initialPhysicalDefense = 234, // modified so it won't be the same as magical defense
      initialMagicalDefense = 43,
    )
  private val s = TestScenario.generate(TestHexMapName.Simple1v1, characterMetadata)
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
