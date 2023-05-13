package unit.abilities.llenn

import com.tosware.nkm.*
import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.abilities.llenn.RunItDown
import com.tosware.nkm.models.game.hex.TestHexMapName
import helpers.{TestScenario, TestUtils}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class RunItDownSpec
  extends AnyWordSpecLike
    with Matchers
    with TestUtils
{
  private val abilityMetadata = RunItDown.metadata
  private val s = TestScenario.generate(TestHexMapName.Simple2v2, abilityMetadata.id)
  private val gameState: GameState = s.gameState.incrementPhase(4)
  private val abilityId = s.p(0)(1).character.state.abilities.head.id
  private val aGs: GameState = gameState.useAbility(abilityId)

  RunItDown.metadata.name must {
    "be able to use" in {
      assertCommandSuccess {
        GameStateValidator()(gameState)
          .validateAbilityUse(s.owners(0), abilityId)
      }
    }

    "be able to move and attack three times after using" in {
      def validateBasicAttack(gameState: GameState) =
        GameStateValidator()(gameState)
          .validateBasicAttackCharacter(
            s.owners(0),
            s.p(0)(1).character.id,
            s.p(1)(0).character.id,
          )

      def validateBasicMove(gameState: GameState, cs: Seq[(Int, Int)]) =
        GameStateValidator()(gameState).validateBasicMoveCharacter(s.owners(0),
          CoordinateSeq(cs *),
          s.p(0)(1).character.id
        )

      def basicAttack(gameState: GameState) =
        gameState.basicAttack(s.p(0)(1).character.id, s.p(1)(0).character.id)

      def basicMove(gameState: GameState, cs: Seq[(Int, Int)]) =
        gameState.basicMoveCharacter(s.p(0)(1).character.id, CoordinateSeq(cs *))

      assertCommandSuccess (validateBasicMove(aGs, Seq((0, 0), (1, 0))))
      assertCommandSuccess (validateBasicAttack(aGs))

      val g1 = basicMove(aGs, Seq((0, 0), (1, 0)))
      assertCommandSuccess (validateBasicMove(g1, Seq((1, 0), (0, 0))))
      assertCommandSuccess (validateBasicAttack(g1))

      val g2 = basicAttack(g1)
      assertCommandSuccess (validateBasicMove(g2, Seq((1, 0), (0, 0))))
      assertCommandFailure (validateBasicAttack(g2))

      val g3 = basicMove(g2, Seq((1, 0), (0, 0)))
      assertCommandSuccess (validateBasicMove(g3, Seq((0, 0), (1, 0))))
      assertCommandSuccess (validateBasicAttack(g3))

      val g4 = basicAttack(g3)
      assertCommandSuccess (validateBasicMove(g4, Seq((0, 0), (1, 0))))
      assertCommandFailure (validateBasicAttack(g4))

      val g5 = basicMove(g4, Seq((0, 0), (1, 0)))
      assertCommandFailure (validateBasicMove(g5, Seq((1, 0), (0, 0))))
      assertCommandSuccess (validateBasicAttack(g5))

      val g6 = basicAttack(g5)
      assertCommandFailure (validateBasicMove(g6, Seq((1, 0), (0, 0))))
      assertCommandFailure (validateBasicAttack(g6))
    }
  }
}