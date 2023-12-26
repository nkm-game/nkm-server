package unit.abilities.blank

import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game.abilities.blank.Castling
import com.tosware.nkm.models.game.ability.UseData
import com.tosware.nkm.models.game.game_state.GameState
import com.tosware.nkm.models.game.hex.TestHexMapName
import helpers.{TestScenario, TestUtils}

class CastlingSpec extends TestUtils {
  private val abilityMetadata = Castling.metadata
  private val s = TestScenario.generate(TestHexMapName.Simple2v2, abilityMetadata.id)
  private val gameState: GameState = s.ultGs
  private val abilityId = s.defaultAbilityId

  abilityMetadata.name must {
    "be able to use castling on characters on map" in {
      assertCommandSuccess {
        GameStateValidator()(gameState)
          .validateAbilityUse(
            s.owners(0),
            abilityId,
            UseData(Seq(s.defaultEnemy.id, s.p(0)(1).character.id)),
          )
      }
    }
    "not be able to use castling on the same character" in {
      assertCommandFailure {
        GameStateValidator()(gameState)
          .validateAbilityUse(
            s.owners(0),
            abilityId,
            UseData(Seq(s.defaultEnemy.id, s.defaultEnemy.id)),
          )
      }
    }
    "not be able to use castling on character outside map" in {
      val s1 = gameState.removeCharacterFromMap(s.p(0)(1).character.id)
      val s2 = gameState.removeCharacterFromMap(s.defaultEnemy.id)

      assertCommandFailure {
        GameStateValidator()(s1)
          .validateAbilityUse(
            s.owners(0),
            abilityId,
            UseData(Seq(s.defaultEnemy.id, s.p(0)(1).character.id)),
          )
      }

      assertCommandFailure {
        GameStateValidator()(s2)
          .validateAbilityUse(
            s.owners(0),
            abilityId,
            UseData(Seq(s.defaultEnemy.id, s.p(0)(1).character.id)),
          )
      }
    }

    "swap positions with castling" in {
      val ngs = gameState.useAbility(
        abilityId,
        UseData(Seq(s.defaultEnemy.id, s.p(0)(1).character.id)),
      )
      ngs
        .characterById(s.defaultEnemy.id)
        .parentCellOpt(gameState).get
        .coordinates shouldBe s.p(0)(1).character.parentCellOpt(ngs).get.coordinates

      ngs
        .characterById(s.p(0)(1).character.id)
        .parentCellOpt(gameState).get
        .coordinates shouldBe s.defaultEnemy.parentCellOpt(ngs).get.coordinates
    }
  }
}
