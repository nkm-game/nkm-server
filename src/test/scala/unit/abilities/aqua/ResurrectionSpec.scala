package unit.abilities.aqua

import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.abilities.aqua.Resurrection
import com.tosware.nkm.models.game.ability.UseData
import com.tosware.nkm.models.game.hex.TestHexMapName
import helpers.{TestScenario, TestUtils}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import spray.json.*

class ResurrectionSpec
    extends AnyWordSpecLike
    with Matchers
    with TestUtils {
  private val abilityMetadata = Resurrection.metadata
  private val s = TestScenario.generate(TestHexMapName.Simple2v2, abilityMetadata.id)
  private val gameState: GameState = s.ultGs
  private val abilityId = s.defaultAbilityId

  abilityMetadata.name must {
    "be able to use on characters that died in the same phase" in {
      val deadGameState =
        gameState.damageCharacter(s.p(0)(1).character.id, Damage(DamageType.True, 99999))(random, gameState.id)

      val r = GameStateValidator()(deadGameState)
        .validateAbilityUse(
          s.owners(0),
          abilityId,
          UseData(Seq(s.p(0)(1).spawnCoordinates.toJson.toString, s.p(0)(1).character.id)),
        )
      assertCommandSuccess(r)
    }
    "be able to resurrect characters that died in the same phase" in {
      val deadGameState =
        gameState.damageCharacter(s.p(0)(1).character.id, Damage(DamageType.True, 99999))(random, gameState.id)

      val resurrectedGameState: GameState = deadGameState.useAbility(
        abilityId,
        UseData(Seq(s.p(0)(1).spawnCoordinates.toJson.toString, s.p(0)(1).character.id)),
      )
      val resurrectedCharacter = resurrectedGameState.characterById(s.p(0)(1).character.id)
      resurrectedCharacter.state.healthPoints should be(resurrectedCharacter.state.maxHealthPoints / 2)
      resurrectedCharacter.parentCellOpt(gameState).map(_.coordinates) should be(Some(s.p(0)(1).spawnCoordinates))
    }

    "be able to resurrect characters that died in phase before" in {
      val deadGameState = gameState
        .damageCharacter(s.p(0)(1).character.id, Damage(DamageType.True, 99999))(random, gameState.id)
        .incrementPhase()

      assertCommandSuccess {
        GameStateValidator()(deadGameState)
          .validateAbilityUse(
            s.owners(0),
            abilityId,
            UseData(Seq(s.p(0)(1).spawnCoordinates.toJson.toString, s.p(0)(1).character.id)),
          )
      }
    }

    "not be able to resurrect characters that died two phases ago" in {
      val deadGameState = gameState
        .damageCharacter(s.p(0)(1).character.id, Damage(DamageType.True, 99999))(random, gameState.id)
        .incrementPhase(2)

      assertCommandFailure {
        GameStateValidator()(deadGameState)
          .validateAbilityUse(
            s.owners(0),
            abilityId,
            UseData(Seq(s.p(0)(1).spawnCoordinates.toJson.toString, s.p(0)(1).character.id)),
          )
      }
    }

    "not be able to resurrect foreign characters" in {
      val deadGameState = gameState
        .damageCharacter(s.p(1)(1).character.id, Damage(DamageType.True, 99999))(random, gameState.id)
        .removeCharacterFromMap(s.p(0)(1).character.id)(random, gameState.id)

      assertCommandFailure {
        GameStateValidator()(deadGameState)
          .validateAbilityUse(
            s.owners(0),
            abilityId,
            UseData(Seq(s.p(1)(1).spawnCoordinates.toJson.toString, s.p(1)(1).character.id)),
          )
      }

      assertCommandFailure {
        GameStateValidator()(deadGameState)
          .validateAbilityUse(
            s.owners(0),
            abilityId,
            UseData(Seq(s.p(0)(1).spawnCoordinates.toJson.toString, s.p(1)(1).character.id)),
          )
      }
    }

    "not be able to resurrect on foreign spawn" in {
      val deadGameState = gameState
        .damageCharacter(s.p(0)(1).character.id, Damage(DamageType.True, 99999))(random, gameState.id)
        .removeCharacterFromMap(s.defaultEnemy.id)(random, gameState.id)
      assertCommandFailure {
        GameStateValidator()(deadGameState)
          .validateAbilityUse(
            s.owners(0),
            abilityId,
            UseData(Seq(s.p(1)(0).spawnCoordinates.toJson.toString, s.p(0)(1).character.id)),
          )
      }
    }
    "not be able to resurrect on tiles that are not free to stand" in {
      val deadGameState = gameState
        .damageCharacter(s.defaultCharacter.id, Damage(DamageType.True, 99999))(random, gameState.id)

      assertCommandFailure {
        GameStateValidator()(deadGameState)
          .validateAbilityUse(
            s.owners(0),
            abilityId,
            UseData(Seq(s.p(0)(1).spawnCoordinates.toJson.toString, s.defaultCharacter.id)),
          )
      }
    }

    "not be able to resurrect characters that are alive" in {
      val aliveGameState = gameState
        .removeCharacterFromMap(s.p(0)(1).character.id)(random, gameState.id)

      assertCommandFailure {
        GameStateValidator()(aliveGameState)
          .validateAbilityUse(
            s.owners(0),
            abilityId,
            UseData(Seq(s.p(0)(1).spawnCoordinates.toJson.toString, s.p(0)(1).character.id)),
          )
      }
    }
  }
}
