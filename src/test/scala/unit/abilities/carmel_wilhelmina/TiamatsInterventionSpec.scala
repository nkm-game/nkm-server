package unit.abilities.carmel_wilhelmina

import com.tosware.nkm.*
import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.abilities.carmel_wilhelmina.TiamatsIntervention
import com.tosware.nkm.models.game.ability.UseData
import com.tosware.nkm.models.game.character.CharacterMetadata
import com.tosware.nkm.models.game.hex.HexCoordinates
import helpers.{TestUtils, scenarios}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import spray.json.*

class TiamatsInterventionSpec
    extends AnyWordSpecLike
    with Matchers
    with TestUtils {
  private val abilityMetadata = TiamatsIntervention.metadata
  private val characterMetadata = CharacterMetadata.empty().copy(initialAbilitiesMetadataIds = Seq(abilityMetadata.id))
  private val s = scenarios.Simple2v2TestScenario(characterMetadata)
  implicit private val gameState: GameState = s.gameState.incrementPhase(4)
  private val abilityId = s.p(0)(0).character.state.abilities.head.id

  abilityMetadata.name must {
    "not be able to use when there are no tiles nearby to pull" in {
      val s = scenarios.Simple1v9LineTestScenario(characterMetadata)
      val gs = s.gameState.incrementPhase(4)
      val abilityId = s.p(0)(0).character.state.abilities.head.id
      assertCommandFailure {
        GameStateValidator()(gs)
          .validateAbilityUseOnCharacter(
            s.p(0)(0).ownerId,
            abilityId,
            s.p(1)(0).character.id,
            UseData(HexCoordinates(1, 0).toJson.toString),
          )
      }
    }
    "not be able to use on cell that is not free to stand" in {
      val s = scenarios.Simple1v9LineTestScenario(characterMetadata)
      val gs = s.gameState.incrementPhase(4).executeCharacter(s.p(1)(1).character.id)(random, "test")
      val abilityId = s.p(0)(0).character.state.abilities.head.id
      assertCommandFailure {
        GameStateValidator()(gs)
          .validateAbilityUseOnCharacter(
            s.p(0)(0).ownerId,
            abilityId,
            s.p(1)(0).character.id,
            UseData(HexCoordinates(1, 0).toJson.toString),
          )
      }
    }
    "be able to pull allies and give them shield" in {
      assertCommandSuccess {
        GameStateValidator()
          .validateAbilityUseOnCharacter(
            s.p(0)(0).character.owner.id,
            abilityId,
            s.p(0)(1).character.id,
            UseData(HexCoordinates(1, 0).toJson.toString),
          )
      }

      val newGameState = gameState.useAbilityOnCharacter(
        abilityId,
        s.p(0)(1).character.id,
        UseData(HexCoordinates(1, 0).toJson.toString),
      )
      val targetCharacter = newGameState.characterById(s.p(0)(1).character.id)
      targetCharacter.parentCell(newGameState).get.coordinates should be(HexCoordinates(1, 0))
      targetCharacter.state.shield should be > 0
      targetCharacter.state.effects.ofType[effects.Stun].size should be(0)
    }
    "be able to pull enemies and stun them" in {
      assertCommandSuccess {
        GameStateValidator()
          .validateAbilityUseOnCharacter(
            s.p(0)(0).character.owner.id,
            abilityId,
            s.p(1)(0).character.id,
            UseData(HexCoordinates(1, 0).toJson.toString),
          )
      }

      val newGameState = gameState.useAbilityOnCharacter(
        abilityId,
        s.p(1)(0).character.id,
        UseData(HexCoordinates(1, 0).toJson.toString),
      )
      val targetCharacter = newGameState.characterById(s.p(1)(0).character.id)
      targetCharacter.parentCell(newGameState).get.coordinates should be(HexCoordinates(1, 0))
      targetCharacter.state.shield should be(0)
      targetCharacter.state.effects.ofType[effects.Stun].size should be > 0
    }
  }
}
