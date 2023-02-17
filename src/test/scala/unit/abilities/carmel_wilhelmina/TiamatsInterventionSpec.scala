package unit.abilities.carmel_wilhelmina

import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.abilities.carmel_wilhelmina.TiamatsIntervention
import com.tosware.nkm.models.game.ability.UseData
import com.tosware.nkm.models.game.character.CharacterMetadata
import com.tosware.nkm.models.game.hex.HexCoordinates
import helpers.{TestUtils, scenarios}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import spray.json._

class TiamatsInterventionSpec
  extends AnyWordSpecLike
    with Matchers
    with TestUtils
{
  private val abilityMetadata = TiamatsIntervention.metadata
  private val characterMetadata = CharacterMetadata.empty().copy(initialAbilitiesMetadataIds = Seq(abilityMetadata.id))
  private val s = scenarios.Simple2v2TestScenario(characterMetadata)
  private implicit val gameState: GameState = s.gameState.incrementPhase(4)
  private val abilityId = s.characters.p0First.state.abilities.head.id

  abilityMetadata.name must {
    "not be able to use when there are no tiles nearby to pull" in {
      val s = scenarios.Simple1v9LineTestScenario(characterMetadata)
      val gs = s.gameState.incrementPhase(4)
      val abilityId = s.characters.p0.state.abilities.head.id
      assertCommandFailure {
        GameStateValidator()(gs)
          .validateAbilityUseOnCharacter(
            s.characters.p0.owner(gs).id,
            abilityId,
            s.characters.p1.head.id,
            UseData(HexCoordinates(1, 0).toJson.toString),
          )
      }
    }
    "not be able to use on cell that is not free to stand" in {
      val s = scenarios.Simple1v9LineTestScenario(characterMetadata)
      val gs = s.gameState.incrementPhase(4).executeCharacter(s.characters.p1(1).id)(random, "test")
      val abilityId = s.characters.p0.state.abilities.head.id
      assertCommandFailure {
        GameStateValidator()(gs)
          .validateAbilityUseOnCharacter(
            s.characters.p0.owner(gs).id,
            abilityId,
            s.characters.p1.head.id,
            UseData(HexCoordinates(1, 0).toJson.toString),
          )
      }
    }
    "be able to pull allies and give them shield" in {
      assertCommandSuccess {
        GameStateValidator()
          .validateAbilityUseOnCharacter(
            s.characters.p0First.owner.id,
            abilityId,
            s.characters.p0Second.id,
            UseData(HexCoordinates(1, 0).toJson.toString),
          )
      }

      val newGameState = gameState.useAbilityOnCharacter(
        abilityId,
        s.characters.p0Second.id,
        UseData(HexCoordinates(1, 0).toJson.toString),
      )
      val targetCharacter = newGameState.characterById(s.characters.p0Second.id)
      targetCharacter.parentCell(newGameState).get.coordinates should be (HexCoordinates(1, 0))
      targetCharacter.state.shield should be > 0
      targetCharacter.state.effects.ofType[effects.Stun].size should be (0)
    }
    "be able to pull enemies and stun them" in {
      assertCommandSuccess {
        GameStateValidator()
          .validateAbilityUseOnCharacter(
            s.characters.p0First.owner.id,
            abilityId,
            s.characters.p1First.id,
            UseData(HexCoordinates(1, 0).toJson.toString),
          )
      }

      val newGameState = gameState.useAbilityOnCharacter(
        abilityId,
        s.characters.p1First.id,
        UseData(HexCoordinates(1, 0).toJson.toString),
      )
      val targetCharacter = newGameState.characterById(s.characters.p1First.id)
      targetCharacter.parentCell(newGameState).get.coordinates should be (HexCoordinates(1, 0))
      targetCharacter.state.shield should be (0)
      targetCharacter.state.effects.ofType[effects.Stun].size should be > 0
    }
  }
}