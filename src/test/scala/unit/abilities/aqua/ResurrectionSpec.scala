package unit.abilities.aqua

import com.tosware.nkm.models.game.abilities.aqua.Resurrection
import com.tosware.nkm.models.game.ability.UseData
import com.tosware.nkm.models.game.hex.HexCoordinates
import com.tosware.nkm.models.game.{Damage, DamageType, GameState}
import com.tosware.nkm.models.game.character.CharacterMetadata
import com.tosware.nkm.models.GameStateValidator
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
  private val abilityId = s.characters.p0First.state.abilities.head.id

  abilityMetadata.name must {
    "be able to use on characters that died in the same phase" in {
      val deadGameState = gameState.damageCharacter(s.characters.p0Second.id, Damage(DamageType.True, 99999))(random, gameState.id)

      val r = GameStateValidator()(deadGameState)
        .validateAbilityUseOnCoordinates(
          s.characters.p0First.owner(gameState).id,
          abilityId,
          s.spawnCoordinates.p0Second,
          UseData(s.characters.p0Second.id),
        )
      assertCommandSuccess(r)
    }
    "be able to resurrect characters that died in the same phase" in {
      val deadGameState = gameState.damageCharacter(s.characters.p0Second.id, Damage(DamageType.True, 99999))(random, gameState.id)

      val resurrectedGameState: GameState = deadGameState.useAbilityOnCoordinates(
        abilityId,
        s.spawnCoordinates.p0Second,
        UseData(s.characters.p0Second.id),
      )
      val resurrectedCharacter = resurrectedGameState.characterById(s.characters.p0Second.id)
      resurrectedCharacter.state.healthPoints should be (resurrectedCharacter.state.maxHealthPoints / 2)
      resurrectedCharacter.parentCell(gameState).map(_.coordinates) should be (Some(HexCoordinates(-1, 0)))
    }

    "be able to resurrect characters that died in phase before" in {
      val deadGameState = gameState
        .damageCharacter(s.characters.p0Second.id, Damage(DamageType.True, 99999))(random, gameState.id)
        .incrementPhase()

      val r = GameStateValidator()(deadGameState)
        .validateAbilityUseOnCoordinates(
          s.characters.p0First.owner(gameState).id,
          abilityId,
          s.spawnCoordinates.p0Second,
          UseData(s.characters.p0Second.id),
        )
      assertCommandSuccess(r)
    }

    "not be able to resurrect characters that died two phases ago" in {
      val deadGameState = gameState
        .damageCharacter(s.characters.p0Second.id, Damage(DamageType.True, 99999))(random, gameState.id)
        .incrementPhase(2)

      val r = GameStateValidator()(deadGameState)
        .validateAbilityUseOnCoordinates(
          s.characters.p0First.owner(gameState).id,
          abilityId,
          s.spawnCoordinates.p0Second,
          UseData(s.characters.p0Second.id),
        )
      assertCommandFailure(r)
    }

    "not be able to resurrect foreign characters" in {
      val deadGameState = gameState
        .damageCharacter(s.characters.p1Second.id, Damage(DamageType.True, 99999))(random, gameState.id)
        .removeCharacterFromMap(s.characters.p0Second.id)(random, gameState.id)

      val r = GameStateValidator()(deadGameState)
        .validateAbilityUseOnCoordinates(
          s.characters.p0First.owner(gameState).id,
          abilityId,
          s.spawnCoordinates.p1Second,
          UseData(s.characters.p1Second.id),
        )
      assertCommandFailure(r)

      val r2 = GameStateValidator()(deadGameState)
        .validateAbilityUseOnCoordinates(
          s.characters.p0First.owner(gameState).id,
          abilityId,
          s.spawnCoordinates.p0Second,
          UseData(s.characters.p1Second.id),
        )
      assertCommandFailure(r2)
    }

    "not be able to resurrect on foreign spawn" in {
      val deadGameState = gameState
        .damageCharacter(s.characters.p0Second.id, Damage(DamageType.True, 99999))(random, gameState.id)
        .removeCharacterFromMap(s.characters.p1First.id)(random, gameState.id)

      val r = GameStateValidator()(deadGameState)
        .validateAbilityUseOnCoordinates(
          s.characters.p0First.owner(gameState).id,
          abilityId,
          s.spawnCoordinates.p1First,
          UseData(s.characters.p0Second.id),
        )
      assertCommandFailure(r)
    }
    "not be able to resurrect on tiles that are not free to stand" in {
      val deadGameState = gameState
        .damageCharacter(s.characters.p0Second.id, Damage(DamageType.True, 99999))(random, gameState.id)

      val r = GameStateValidator()(deadGameState)
        .validateAbilityUseOnCoordinates(
          s.characters.p0First.owner(gameState).id,
          abilityId,
          s.spawnCoordinates.p0First,
          UseData(s.characters.p0Second.id),
        )
      assertCommandFailure(r)
    }

    "not be able to resurrect characters that are alive" in {
      val aliveGameState = gameState
        .removeCharacterFromMap(s.characters.p0Second.id)(random, gameState.id)

      val r = GameStateValidator()(aliveGameState)
        .validateAbilityUseOnCoordinates(
          s.characters.p0First.owner(gameState).id,
          abilityId,
          s.spawnCoordinates.p0Second,
          UseData(s.characters.p0Second.id),
        )
      assertCommandFailure(r)
    }
  }

}