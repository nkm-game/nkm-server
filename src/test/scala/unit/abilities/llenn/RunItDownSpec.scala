package unit.abilities.llenn

import com.tosware.NKM.models.GameStateValidator
import com.tosware.NKM.models.game._
import com.tosware.NKM.models.game.abilities.llenn.RunItDown
import com.tosware.NKM.models.game.hex.HexCoordinates
import com.tosware.NKM.models.game.hex.HexUtils.CoordinateSeq
import com.tosware.NKM.providers.HexMapProvider.TestHexMapName
import helpers.TestUtils
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class RunItDownSpec
  extends AnyWordSpecLike
    with Matchers
    with TestUtils
{
  val metadata = CharacterMetadata.empty().copy(initialAbilitiesMetadataIds = Seq(RunItDown.metadata.id))
  implicit val gameState = getTestGameState(TestHexMapName.Simple2v2, Seq(
    Seq(metadata.copy(name = "Empty1"), metadata.copy(name = "Empty2")),
    Seq(metadata.copy(name = "Empty3"), metadata.copy(name = "Empty4")),
  ))

  val p0FirstCharacterSpawnCoordinates = HexCoordinates(0, 0)
  val p0SecondCharacterSpawnCoordinates = HexCoordinates(-1, 0)
  val p1FirstCharacterSpawnCoordinates = HexCoordinates(3, 0)
  val p1SecondCharacterSpawnCoordinates = HexCoordinates(4, 0)

  val p0FirstCharacter = characterOnPoint(p0FirstCharacterSpawnCoordinates)
  val p0SecondCharacter = characterOnPoint(p0SecondCharacterSpawnCoordinates)

  val p1FirstCharacter = characterOnPoint(p1FirstCharacterSpawnCoordinates)
  val p1SecondCharacter = characterOnPoint(p1SecondCharacterSpawnCoordinates)

  val abilityId = p0FirstCharacter.state.abilities.head.id
  val pid = gameState.players(0).id

  RunItDown.metadata.name must {
    "be able to move and attack three times after using" in {
      {
        val r = GameStateValidator().validateAbilityUseWithoutTarget(p0FirstCharacter.owner.id, abilityId)
        assertCommandSuccess(r)
      }
      val abilityUsedGameState: GameState = gameState.useAbilityWithoutTarget(abilityId)

      def validateBasicAttack(gameState: GameState) =
        GameStateValidator()(gameState).validateBasicAttackCharacter(pid,
          p0FirstCharacter.id,
          p1FirstCharacter.id,
        )

      def validateBasicMove(gameState: GameState, cs: Seq[(Int, Int)]) =
        GameStateValidator()(gameState).validateBasicMoveCharacter(pid,
          CoordinateSeq(cs: _*),
          p0FirstCharacter.id
        )

      def basicAttack(gameState: GameState) =
        gameState.basicAttack(p0FirstCharacter.id, p1FirstCharacter.id)

      def basicMove(gameState: GameState, cs: Seq[(Int, Int)]) =
        gameState.basicMoveCharacter(CoordinateSeq(cs: _*), p0FirstCharacter.id)

      assertCommandSuccess (validateBasicMove(abilityUsedGameState, Seq((0, 0), (1, 0))))
      assertCommandSuccess (validateBasicAttack(abilityUsedGameState))

      val g1 = basicMove(abilityUsedGameState, Seq((0, 0), (1, 0)))
      assertCommandSuccess (validateBasicMove(g1, Seq((1, 0), (0, 0))))
      assertCommandSuccess (validateBasicAttack(g1))

      val g2 = basicAttack(g1)
      assertCommandSuccess (validateBasicMove(g2, Seq((1, 0), (0, 0))))
      assertCommandFailure (validateBasicAttack(g2))

      val g3 = basicMove(g2, Seq((0, 0), (1, 0)))
      assertCommandSuccess (validateBasicMove(g3, Seq((1, 0), (0, 0))))
      assertCommandSuccess (validateBasicAttack(g3))

      val g4 = basicAttack(g3)
      assertCommandSuccess (validateBasicMove(g4, Seq((1, 0), (0, 0))))
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