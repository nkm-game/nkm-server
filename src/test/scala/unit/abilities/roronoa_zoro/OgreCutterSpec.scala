package unit.abilities.roronoa_zoro

import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.abilities.roronoa_zoro.OgreCutter
import com.tosware.nkm.models.game.hex.HexCoordinates
import com.tosware.nkm.models.game.hex.HexUtils.SeqUtils
import helpers.{TestUtils, scenarios}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class OgreCutterSpec
  extends AnyWordSpecLike
    with Matchers
    with TestUtils
{
  private val metadata = CharacterMetadata.empty().copy(initialAbilitiesMetadataIds = Seq(OgreCutter.metadata.id))
  private val s = scenarios.OgreCutterTestScenario(metadata)
  private implicit val gameState: GameState = s.gameState
  private val abilityId = s.characters.p0.state.abilities.head.id

  OgreCutter.metadata.name must {
    "be able to damage and teleport" in {
      val r = GameStateValidator()
        .validateAbilityUseOnCharacter(s.characters.p0.owner.id, abilityId, s.characters.p1.id)
      assertCommandSuccess(r)

      val newGameState: GameState = gameState.useAbilityOnCharacter(abilityId, s.characters.p1.id)
      newGameState.gameLog.events.ofType[GameEvent.CharacterDamaged].exists(_.causedById == abilityId)
      newGameState.hexMap.get.getCellOfCharacter(s.characters.p0.id).get.coordinates.toTuple shouldBe (5, 0)
    }
    "not be able to use if teleport cell is not free to stand" in {
      val newGameState = gameState.teleportCharacter(s.characters.p1.id, HexCoordinates(4, 0))(random, gameState.id)
      val r = GameStateValidator()(newGameState)
        .validateAbilityUseOnCharacter(s.characters.p0.owner.id, abilityId, s.characters.p1.id)
      assertCommandFailure(r)
    }
  }
}