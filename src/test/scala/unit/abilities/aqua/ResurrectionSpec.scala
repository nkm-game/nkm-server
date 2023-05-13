package unit.abilities.aqua

import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game.abilities.aqua.Resurrection
import com.tosware.nkm.models.game.ability.UseData
import com.tosware.nkm.models.game.character.CharacterMetadata
import com.tosware.nkm.models.game.*
import helpers.{TestUtils, scenarios}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class ResurrectionSpec
  extends AnyWordSpecLike
    with Matchers
    with TestUtils
{
  private val abilityMetadata = Resurrection.metadata
  private val metadata = CharacterMetadata.empty().copy(initialAbilitiesMetadataIds = Seq(abilityMetadata.id))
  private val s = scenarios.Simple2v2TestScenario(metadata)
  private val gameState: GameState = s.gameState.incrementPhase(4)
  private val abilityId = s.p(0)(0).character.state.abilities.head.id

  abilityMetadata.name must {
    "be able to use on characters that died in the same phase" in {
      val deadGameState = gameState.damageCharacter(s.p(0)(1).character.id, Damage(DamageType.True, 99999))(random, gameState.id)

      val r = GameStateValidator()(deadGameState)
        .validateAbilityUseOnCoordinates(
          s.p(0)(0).ownerId,
          abilityId,
          s.p(0)(1).spawnCoordinates,
          UseData(s.p(0)(1).character.id),
        )
      assertCommandSuccess(r)
    }
    "be able to resurrect characters that died in the same phase" in {
      val deadGameState = gameState.damageCharacter(s.p(0)(1).character.id, Damage(DamageType.True, 99999))(random, gameState.id)

      val resurrectedGameState: GameState = deadGameState.useAbilityOnCoordinates(
        abilityId,
        s.p(0)(1).spawnCoordinates,
        UseData(s.p(0)(1).character.id),
      )
      val resurrectedCharacter = resurrectedGameState.characterById(s.p(0)(1).character.id)
      resurrectedCharacter.state.healthPoints should be (resurrectedCharacter.state.maxHealthPoints / 2)
      resurrectedCharacter.parentCell(gameState).map(_.coordinates) should be (Some(s.p(0)(1).spawnCoordinates))
    }

    "be able to resurrect characters that died in phase before" in {
      val deadGameState = gameState
        .damageCharacter(s.p(0)(1).character.id, Damage(DamageType.True, 99999))(random, gameState.id)
        .incrementPhase()

      val r = GameStateValidator()(deadGameState)
        .validateAbilityUseOnCoordinates(
          s.p(0)(0).ownerId,
          abilityId,
          s.p(0)(1).spawnCoordinates,
          UseData(s.p(0)(1).character.id),
        )
      assertCommandSuccess(r)
    }

    "not be able to resurrect characters that died two phases ago" in {
      val deadGameState = gameState
        .damageCharacter(s.p(0)(1).character.id, Damage(DamageType.True, 99999))(random, gameState.id)
        .incrementPhase(2)

      val r = GameStateValidator()(deadGameState)
        .validateAbilityUseOnCoordinates(
          s.p(0)(0).ownerId,
          abilityId,
          s.p(0)(1).spawnCoordinates,
          UseData(s.p(0)(1).character.id),
        )
      assertCommandFailure(r)
    }

    "not be able to resurrect foreign characters" in {
      val deadGameState = gameState
        .damageCharacter(s.p(1)(1).character.id, Damage(DamageType.True, 99999))(random, gameState.id)
        .removeCharacterFromMap(s.p(0)(1).character.id)(random, gameState.id)

      val r = GameStateValidator()(deadGameState)
        .validateAbilityUseOnCoordinates(
          s.p(0)(0).ownerId,
          abilityId,
          s.p(1)(1).spawnCoordinates,
          UseData(s.p(1)(1).character.id),
        )
      assertCommandFailure(r)

      val r2 = GameStateValidator()(deadGameState)
        .validateAbilityUseOnCoordinates(
          s.p(0)(0).ownerId,
          abilityId,
          s.p(0)(1).spawnCoordinates,
          UseData(s.p(1)(1).character.id),
        )
      assertCommandFailure(r2)
    }

    "not be able to resurrect on foreign spawn" in {
      val deadGameState = gameState
        .damageCharacter(s.p(0)(1).character.id, Damage(DamageType.True, 99999))(random, gameState.id)
        .removeCharacterFromMap(s.p(1)(0).character.id)(random, gameState.id)

      val r = GameStateValidator()(deadGameState)
        .validateAbilityUseOnCoordinates(
          s.p(0)(0).ownerId,
          abilityId,
          s.p(1)(0).spawnCoordinates,
          UseData(s.p(0)(1).character.id),
        )
      assertCommandFailure(r)
    }
    "not be able to resurrect on tiles that are not free to stand" in {
      val deadGameState = gameState
        .damageCharacter(s.p(0)(0).character.id, Damage(DamageType.True, 99999))(random, gameState.id)

      val r = GameStateValidator()(deadGameState)
        .validateAbilityUseOnCoordinates(
          s.p(0)(1).ownerId,
          abilityId,
          s.p(0)(1).spawnCoordinates,
          UseData(s.p(0)(0).character.id),
        )
      assertCommandFailure(r)
    }

    "not be able to resurrect characters that are alive" in {
      val aliveGameState = gameState
        .removeCharacterFromMap(s.p(0)(1).character.id)(random, gameState.id)

      val r = GameStateValidator()(aliveGameState)
        .validateAbilityUseOnCoordinates(
          s.p(0)(0).ownerId,
          abilityId,
          s.p(0)(1).spawnCoordinates,
          UseData(s.p(0)(1).character.id),
        )
      assertCommandFailure(r)
    }
  }

}