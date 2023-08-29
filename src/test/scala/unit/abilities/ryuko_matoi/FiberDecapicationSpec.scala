package unit.abilities.ryuko_matoi

import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game.GameState
import com.tosware.nkm.models.game.abilities.ryuko_matoi.{FiberDecapitation, ScissorBlade}
import com.tosware.nkm.models.game.character.CharacterMetadata
import com.tosware.nkm.models.game.effects.StatNerf
import com.tosware.nkm.models.game.event.GameEvent
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
  private val characterMetadata = CharacterMetadata.empty().copy(initialAbilitiesMetadataIds = Seq(abilityMetadata.id, ScissorBlade.metadata.id))
  private val s = scenarios.FiberDecapicationTestScenario(characterMetadata)
  private implicit val gameState: GameState = s.gameState
  private val abilityId = s.p(0)(0).character.state.abilities.head.id

  abilityMetadata.name must {
    "be able to use" in {
      val r = GameStateValidator()
        .validateAbilityUseOnCharacter(s.p(0)(0).character.owner.id, abilityId, s.p(1)(0).character.id)
      assertCommandSuccess(r)
    }

    "not be able to use if teleport cell is not free to stand" in {
      val newGameState = gameState.teleportCharacter(s.p(1)(0).character.id, HexCoordinates(4, 0))(random, gameState.id)
      val r = GameStateValidator()(newGameState)
        .validateAbilityUseOnCharacter(s.p(0)(0).character.owner.id, abilityId, s.p(1)(0).character.id)
      assertCommandFailure(r)
    }

    "not be able to use if teleport cell does not exist" in {
      val newGameState = gameState.teleportCharacter(s.p(1)(0).character.id, HexCoordinates(5, 0))(random, gameState.id)
      val r = GameStateValidator()(newGameState)
        .validateAbilityUseOnCharacter(s.p(0)(0).character.owner.id, abilityId, s.p(1)(0).character.id)
      assertCommandFailure(r)
    }

    "be able to decrease physical defense, damage and teleport" in {
      val oldPhysicalDefense = gameState.characterById(s.p(1)(0).character.id).state.purePhysicalDefense

      val newGameState: GameState = gameState.useAbilityOnCharacter(abilityId, s.p(1)(0).character.id)
      val newPhysicalDefense = newGameState.characterById(s.p(1)(0).character.id).state.purePhysicalDefense

      oldPhysicalDefense should be > newPhysicalDefense

      newGameState
        .gameLog.events
        .causedBy(abilityId)
        .ofType[GameEvent.CharacterDamaged] should not be empty

      newGameState.hexMap
        .getCellOfCharacter(s.p(0)(0).character.id).get
        .coordinates.toTuple shouldBe (6, 0)
    }
    "not apply basic attack effects" in {
      val abilityUsedGs: GameState = gameState.useAbilityOnCharacter(abilityId, s.p(1)(0).character.id)
      assertEffectDoesNotExistsOfType[StatNerf](s.p(1)(0).character.id)(abilityUsedGs)
    }
  }
}