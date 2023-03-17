package unit.abilities.kazuma

import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.abilities.satou_kazuma.Steal
import com.tosware.nkm.models.game.hex.TestHexMapName
import helpers.{TestScenario, TestUtils}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import scala.annotation.tailrec
import scala.util.Random

class StealSpec
  extends AnyWordSpecLike
    with Matchers
    with TestUtils
{
  private val abilityMetadata = Steal.metadata
  private val s = TestScenario.generate(TestHexMapName.Simple1v1, abilityMetadata.id)
  private val aGs: GameState =
    s.ultGs.useAbilityOnCharacter(s.defaultAbilityId, s.p(1)(0).character.id)

  private def assertStealActive(gs: GameState) = {
    val newMagicalDefense = s.defaultCharacter.state.pureMagicalDefense + s.p(1)(0).character.state.pureMagicalDefense
    val newPhysicalDefense = s.defaultCharacter.state.purePhysicalDefense + s.p(1)(0).character.state.purePhysicalDefense

    gs.characterById(s.defaultCharacter.id).state.pureMagicalDefense should be (newMagicalDefense)
    gs.characterById(s.defaultCharacter.id).state.purePhysicalDefense should be (newPhysicalDefense)

    gs.characterById(s.p(1)(0).character.id).state.pureMagicalDefense should be (0)
    gs.characterById(s.p(1)(0).character.id).state.purePhysicalDefense should be (0)
  }

  private def assertStealInactive(gs: GameState) = {
    gs.characterById(s.defaultCharacter.id).state.pureMagicalDefense should be (s.defaultCharacter.state.pureMagicalDefense)
    gs.characterById(s.defaultCharacter.id).state.purePhysicalDefense should be (s.defaultCharacter.state.purePhysicalDefense)

    gs.characterById(s.p(1)(0).character.id).state.pureMagicalDefense should be (s.p(1)(0).character.state.pureMagicalDefense)
    gs.characterById(s.p(1)(0).character.id).state.purePhysicalDefense should be (s.p(1)(0).character.state.purePhysicalDefense)
  }

  def passAllCharactersInNPhases(gs: GameState, n: Int)(implicit random: Random): GameState =
    Function.chain(List.fill(n)(passAllCharactersInCurrentPhase))(gs)

  def passAllCharactersInCurrentPhase(gs: GameState)(implicit random: Random): GameState =
    passAllCharactersInPhase(gs, gs.phase.number)


  @tailrec
  final def passAllCharactersInPhase(gs: GameState, phaseNumber: Int)(implicit random: Random): GameState =
  {
    val ngs = gs.characterTakingActionThisTurn.fold(gs)(_ => gs.endTurn())
    if(ngs.phase.number != phaseNumber) return ngs

    val charactersToPass = ngs.currentPlayer.characterIds.intersect(ngs.charactersToTakeAction)
    passAllCharactersInPhase(ngs.passTurn(charactersToPass.head), phaseNumber)
  }

  abilityMetadata.name must {
    "be able to use" in {
      assertCommandSuccess {
        GameStateValidator()(s.ultGs)
          .validateAbilityUseOnCharacter(s.owners(0), s.defaultAbilityId, s.p(1)(0).character.id)
      }
    }

    "be able to steal armor" in {
      assertStealActive(aGs)
    }

    "restore armor after duration time" in {
      val duration = abilityMetadata.variables("duration")

      (0 until duration).map(n => {
        logger.info(s"Checking phase $n")
        passAllCharactersInNPhases(aGs, n)
      }).foreach(assertStealActive)

      (duration to duration+5).map(n => {
        logger.info(s"Checking phase $n")
        passAllCharactersInNPhases(aGs, n)
      }).foreach(assertStealInactive)
    }
  }
}