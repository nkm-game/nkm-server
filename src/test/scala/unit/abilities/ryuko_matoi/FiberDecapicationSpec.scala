package unit.abilities.ryuko_matoi

import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.abilities.ryuko_matoi.FiberDecapitation
import com.tosware.nkm.models.game.character.CharacterMetadata
import com.tosware.nkm.models.game.hex.HexCoordinates
import helpers.{TestUtils, scenarios}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class FiberDecapicationSpec
  extends AnyWordSpecLike
    with Matchers
    with TestUtils
{
  private val abilityMetadata = FiberDecapitation.metadata
  private val characterMetadata = CharacterMetadata.empty().copy(initialAbilitiesMetadataIds = Seq(abilityMetadata.id))
  private val s = scenarios.FiberDecapicationTestScenario(characterMetadata)
  private implicit val gameState: GameState = s.gameState
  private val abilityId = s.characters.p0.state.abilities.head.id

  abilityMetadata.name must {
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

    "be able to decrease physical defense, damage and teleport" in {
      val oldPhysicalDefense = gameState.characterById(s.characters.p1.id).state.purePhysicalDefense

      val newGameState: GameState = gameState.useAbilityOnCharacter(abilityId, s.characters.p1.id)
      val newPhysicalDefense = newGameState.characterById(s.characters.p1.id).state.purePhysicalDefense

      oldPhysicalDefense should be > newPhysicalDefense

      newGameState
        .gameLog.events
        .causedBy(s.characters.p0.id)
        .ofType[GameEvent.CharacterDamaged] should not be empty

      newGameState.hexMap
        .getCellOfCharacter(s.characters.p0.id).get
        .coordinates.toTuple shouldBe (6, 0)
    }
  }
}