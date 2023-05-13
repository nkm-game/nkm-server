package unit.abilities.blank

import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.abilities.blank.Castling
import com.tosware.nkm.models.game.ability.UseData
import com.tosware.nkm.models.game.character.CharacterMetadata
import helpers.{TestUtils, scenarios}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class CastlingSpec
  extends AnyWordSpecLike
    with Matchers
    with TestUtils
{
  private val abilityMetadata = Castling.metadata
  private val characterMetadata = CharacterMetadata.empty().copy(initialAbilitiesMetadataIds = Seq(abilityMetadata.id))
  private val s = scenarios.Simple2v2TestScenario(characterMetadata)
  private implicit val gameState: GameState = s.gameState.incrementPhase(4)
  private val abilityId = s.p(0)(0).character.state.abilities.head.id

  abilityMetadata.name must {
    "be able to use castling on characters on map" in {
      val r = GameStateValidator()
        .validateAbilityUseOnCharacter(
          s.p(0)(0).character.owner.id,
          abilityId,
          s.p(1)(0).character.id,
          UseData(s.p(0)(1).character.id),
        )
      assertCommandSuccess(r)
    }
    "not be able to use castling on the same character" in {
      val r = GameStateValidator()
        .validateAbilityUseOnCharacter(
          s.p(0)(0).character.owner.id,
          abilityId,
          s.p(1)(0).character.id,
          UseData(s.p(1)(0).character.id),
        )
      assertCommandFailure(r)
    }
    "not be able to use castling on character outside map" in {
      val s1 = gameState.removeCharacterFromMap(s.p(0)(1).character.id)
      val s2 = gameState.removeCharacterFromMap(s.p(1)(0).character.id)

      val r1 = GameStateValidator()(s1)
        .validateAbilityUseOnCharacter(
          s.p(0)(0).character.owner.id,
          abilityId,
          s.p(1)(0).character.id,
          UseData(s.p(0)(1).character.id),
        )
      assertCommandFailure(r1)

      val r2 = GameStateValidator()(s2)
        .validateAbilityUseOnCharacter(
          s.p(0)(0).character.owner.id,
          abilityId,
          s.p(1)(0).character.id,
          UseData(s.p(0)(1).character.id),
        )
      assertCommandFailure(r2)
    }

    "swap positions with castling" in {
      val newGameState = gameState.useAbilityOnCharacter(
        abilityId,
        s.p(1)(0).character.id,
        UseData(s.p(0)(1).character.id),
      )
      newGameState
        .characterById(s.p(1)(0).character.id)
        .parentCell.get
        .coordinates shouldBe s.p(0)(1).character.parentCell(newGameState).get.coordinates

      newGameState
        .characterById(s.p(0)(1).character.id)
        .parentCell.get
        .coordinates shouldBe s.p(1)(0).character.parentCell(newGameState).get.coordinates
    }
  }
}