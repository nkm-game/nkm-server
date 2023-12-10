package unit.abilities.carmel_wilhelmina

import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.abilities.carmel_wilhelmina.TiamatsIntervention
import com.tosware.nkm.models.game.ability.UseData
import com.tosware.nkm.models.game.character.CharacterMetadata
import com.tosware.nkm.models.game.hex.{HexCoordinates, TestHexMapName}
import helpers.{TestScenario, TestUtils}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import spray.json.*

class TiamatsInterventionSpec
    extends AnyWordSpecLike
    with Matchers
    with TestUtils {
  private val abilityMetadata = TiamatsIntervention.metadata
  private val characterMetadata = CharacterMetadata.empty().copy(initialAbilitiesMetadataIds = Seq(abilityMetadata.id))
  private val s = TestScenario.generate(TestHexMapName.Simple2v2, characterMetadata)
  implicit private val gameState: GameState = s.ultGs
  private val abilityId = s.defaultAbilityId

  abilityMetadata.name must {
    "not be able to use when there are no tiles nearby to pull" in {
      val s = TestScenario.generate(TestHexMapName.Simple1v9Line, characterMetadata)
      val gs = s.ultGs
      val abilityId = s.defaultAbilityId
      assertCommandFailure {
        GameStateValidator()(gs)
          .validateAbilityUse(
            s.owners(0),
            abilityId,
            UseData(Seq(s.defaultEnemy.id, HexCoordinates(1, 0).toJson.toString)),
          )
      }
    }
    "not be able to use on cell that is not free to stand" in {
      val s = TestScenario.generate(TestHexMapName.Simple1v9Line, characterMetadata)
      val gs = s.ultGs.executeCharacter(s.p(1)(1).character.id)(random, "test")
      val abilityId = s.defaultAbilityId
      assertCommandFailure {
        GameStateValidator()(gs)
          .validateAbilityUse(
            s.owners(0),
            abilityId,
            UseData(Seq(s.defaultEnemy.id, HexCoordinates(1, 0).toJson.toString)),
          )
      }
    }
    "be able to pull allies and give them shield" in {
      assertCommandSuccess {
        GameStateValidator()
          .validateAbilityUse(
            s.owners(0),
            abilityId,
            UseData(Seq(s.p(0)(1).character.id, HexCoordinates(1, 0).toJson.toString)),
          )
      }

      val newGameState = gameState.useAbility(
        abilityId,
        UseData(Seq(s.p(0)(1).character.id, HexCoordinates(1, 0).toJson.toString)),
      )
      val targetCharacter = newGameState.characterById(s.p(0)(1).character.id)
      targetCharacter.parentCellOpt(newGameState).get.coordinates should be(HexCoordinates(1, 0))
      targetCharacter.state.shield should be > 0
      targetCharacter.state.effects.ofType[effects.Stun].size should be(0)
    }
    "be able to pull enemies and stun them" in {
      assertCommandSuccess {
        GameStateValidator()
          .validateAbilityUse(
            s.owners(0),
            abilityId,
            UseData(Seq(s.defaultEnemy.id, HexCoordinates(1, 0).toJson.toString)),
          )
      }

      val newGameState = gameState.useAbility(
        abilityId,
        UseData(Seq(s.defaultEnemy.id, HexCoordinates(1, 0).toJson.toString)),
      )
      val targetCharacter = newGameState.characterById(s.defaultEnemy.id)
      targetCharacter.parentCellOpt(newGameState).get.coordinates should be(HexCoordinates(1, 0))
      targetCharacter.state.shield should be(0)
      targetCharacter.state.effects.ofType[effects.Stun].size should be > 0
    }
  }
}
