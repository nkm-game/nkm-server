package unit.abilities.blank

import com.tosware.nkm.actors.Game.GameId
import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.abilities.blank.Castling
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
  private implicit val gameState: GameState = s.gameState
  private val abilityId = s.characters.p0First.state.abilities.head.id

  abilityMetadata.name must {
    "be able to use castling on characters on map" in {
      val r = GameStateValidator()
        .validateAbilityUseOnCharacter(
          s.characters.p0First.owner.id,
          abilityId,
          s.characters.p1First.id,
          UseData(s.characters.p0Second.id),
        )
      assertCommandSuccess(r)
    }
    "not be able to use castling on the same character" in {
      val r = GameStateValidator()
        .validateAbilityUseOnCharacter(
          s.characters.p0First.owner.id,
          abilityId,
          s.characters.p1First.id,
          UseData(s.characters.p1First.id),
        )
      assertCommandFailure(r)
    }
    "not be able to use castling on character outside map" in {
      implicit val causedBy: GameId = gameState.id
      val s1 = gameState.removeCharacterFromMap(s.characters.p0Second.id)
      val s2 = gameState.removeCharacterFromMap(s.characters.p1First.id)

      val r1 = GameStateValidator()(s1)
        .validateAbilityUseOnCharacter(
          s.characters.p0First.owner.id,
          abilityId,
          s.characters.p1First.id,
          UseData(s.characters.p0Second.id),
        )
      assertCommandFailure(r1)

      val r2 = GameStateValidator()(s2)
        .validateAbilityUseOnCharacter(
          s.characters.p0First.owner.id,
          abilityId,
          s.characters.p1First.id,
          UseData(s.characters.p0Second.id),
        )
      assertCommandFailure(r2)
    }

    "swap positions with castling" in {
      val newGameState = gameState.useAbilityOnCharacter(
        abilityId,
        s.characters.p1First.id,
        UseData(s.characters.p0Second.id),
      )
      newGameState
        .characterById(s.characters.p1First.id).get
        .parentCell.get
        .coordinates shouldBe s.characters.p0Second.parentCell.get.coordinates

      newGameState
        .characterById(s.characters.p0Second.id).get
        .parentCell.get
        .coordinates shouldBe s.characters.p1First.parentCell.get.coordinates
    }
  }
}