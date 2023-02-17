package unit.abilities.roronoa_zoro

import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.abilities.roronoa_zoro.OgreCutter
import com.tosware.nkm.models.game.character.CharacterMetadata
import com.tosware.nkm.models.game.hex.HexCoordinates
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
    "be able to use" in {
      val r = GameStateValidator()
        .validateAbilityUseOnCharacter(s.characters.p0.owner.id, abilityId, s.characters.p1.id)
      assertCommandSuccess(r)
    }

    "not be able to use if teleport cell is not free to stand" in {
      val newGameState = gameState.teleportCharacter(s.characters.p1.id, HexCoordinates(4, 0))(random, gameState.id)
      val r = GameStateValidator()(newGameState)
        .validateAbilityUseOnCharacter(s.characters.p0.owner.id, abilityId, s.characters.p1.id)
      assertCommandFailure(r)
    }

    "not be able to use if teleport cell does not exist" in {
      val newGameState = gameState.teleportCharacter(s.characters.p1.id, HexCoordinates(5, 0))(random, gameState.id)
      val r = GameStateValidator()(newGameState)
        .validateAbilityUseOnCharacter(s.characters.p0.owner.id, abilityId, s.characters.p1.id)
      assertCommandFailure(r)
    }

    "be able to damage and teleport" in {
      val newGameState: GameState = gameState.useAbilityOnCharacter(abilityId, s.characters.p1.id)
      newGameState
        .gameLog.events
        .causedBy(s.characters.p0.id)
        .ofType[GameEvent.CharacterDamaged] should not be empty
      newGameState.hexMap
        .getCellOfCharacter(s.characters.p0.id).get
        .coordinates.toTuple shouldBe (5, 0)
    }
  }
}