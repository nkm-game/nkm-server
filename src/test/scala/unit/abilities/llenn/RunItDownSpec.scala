package unit.abilities.llenn

import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.abilities.llenn.RunItDown
import com.tosware.nkm.models.game.character.CharacterMetadata
import helpers.{TestUtils, scenarios}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class RunItDownSpec
  extends AnyWordSpecLike
    with Matchers
    with TestUtils
{
  private val metadata = CharacterMetadata.empty().copy(initialAbilitiesMetadataIds = Seq(RunItDown.metadata.id))
  private val s = scenarios.Simple2v2TestScenario(metadata)
  private implicit val gameState: GameState = s.gameState.incrementPhase(4)
  private val abilityId = s.characters.p0First.state.abilities.head.id
  private val pid = gameState.players.head.id

  RunItDown.metadata.name must {
    "be able to use" in {
      val r = GameStateValidator().validateAbilityUse(s.characters.p0First.owner.id, abilityId)
      assertCommandSuccess(r)
    }

    "be able to move and attack three times after using" in {
      val abilityUsedGameState: GameState = gameState.useAbility(abilityId)

      def validateBasicAttack(gameState: GameState) =
        GameStateValidator()(gameState).validateBasicAttackCharacter(pid,
          s.characters.p0First.id,
          s.characters.p1First.id,
        )

      def validateBasicMove(gameState: GameState, cs: Seq[(Int, Int)]) =
        GameStateValidator()(gameState).validateBasicMoveCharacter(pid,
          CoordinateSeq(cs: _*),
          s.characters.p0First.id
        )

      def basicAttack(gameState: GameState) =
        gameState.basicAttack(s.characters.p0First.id, s.characters.p1First.id)

      def basicMove(gameState: GameState, cs: Seq[(Int, Int)]) =
        gameState.basicMoveCharacter(s.characters.p0First.id, CoordinateSeq(cs: _*))

      assertCommandSuccess (validateBasicMove(abilityUsedGameState, Seq((0, 0), (1, 0))))
      assertCommandSuccess (validateBasicAttack(abilityUsedGameState))

      val g1 = basicMove(abilityUsedGameState, Seq((0, 0), (1, 0)))
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