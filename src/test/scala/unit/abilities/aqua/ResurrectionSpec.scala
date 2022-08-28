package unit.abilities.aqua

import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.abilities.aqua.Resurrection
import com.tosware.nkm.models.game.hex.HexCoordinates
import com.tosware.nkm.models.{Damage, DamageType, GameStateValidator}
import helpers.{Simple2v2TestScenario, TestUtils}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class ResurrectionSpec
  extends AnyWordSpecLike
    with Matchers
    with TestUtils
{
  private val metadata = CharacterMetadata.empty().copy(initialAbilitiesMetadataIds = Seq(Resurrection.metadata.id))
  private val s = Simple2v2TestScenario(metadata)
  private val abilityId = s.characters.p0First.state.abilities.head.id

  Resurrection.metadata.name must {
    "be able to resurrect characters that died in the same phase" in {
      val deadGameState = s.gameState.damageCharacter(s.characters.p0Second.id, Damage(DamageType.True, 99999))(random, s.gameState.id)

      val r = GameStateValidator()(deadGameState)
        .validateAbilityUseOnCoordinates(
          s.characters.p0First.owner(s.gameState).id,
          abilityId,
          s.spawnCoordinates.p0Second,
          UseData(s.characters.p0Second.id),
        )
      assertCommandSuccess(r)

      val resurrectedGameState: GameState = deadGameState.useAbilityOnCoordinates(
        abilityId,
        s.spawnCoordinates.p0Second,
        UseData(s.characters.p0Second.id),
      )
      val resurrectedCharacter = resurrectedGameState.characterById(s.characters.p0Second.id).get
      resurrectedCharacter.state.healthPoints should be (resurrectedCharacter.state.maxHealthPoints / 2)
      resurrectedCharacter.parentCell(s.gameState).map(_.coordinates) should be (Some(HexCoordinates(-1, 0)))
    }

    "be able to resurrect characters that died in phase before" in {
      val deadGameState = s.gameState.damageCharacter(s.characters.p0Second.id, Damage(DamageType.True, 99999))(random, s.gameState.id).incrementPhase()

      val r = GameStateValidator()(deadGameState)
        .validateAbilityUseOnCoordinates(s.characters.p0First.owner(s.gameState).id, abilityId, s.spawnCoordinates.p0Second, UseData(s.characters.p0Second.id))
      assertCommandSuccess(r)
    }

    "not be able to resurrect characters that died two phases ago" in {
      val deadGameState = s.gameState
        .damageCharacter(s.characters.p0Second.id, Damage(DamageType.True, 99999))(random, s.gameState.id)
        .incrementPhase(2)

      val r = GameStateValidator()(deadGameState)
        .validateAbilityUseOnCoordinates(
          s.characters.p0First.owner(s.gameState).id,
          abilityId,
          s.spawnCoordinates.p0Second,
          UseData(s.characters.p0Second.id),
        )
      assertCommandFailure(r)
    }

    "not be able to resurrect foreign characters" in {
      val deadGameState = s.gameState
        .damageCharacter(s.characters.p1Second.id, Damage(DamageType.True, 99999))(random, s.gameState.id)
        .removeCharacterFromMap(s.characters.p0Second.id)(random, s.gameState.id)

      val r = GameStateValidator()(deadGameState)
        .validateAbilityUseOnCoordinates(
          s.characters.p0First.owner(s.gameState).id,
          abilityId,
          s.spawnCoordinates.p1Second,
          UseData(s.characters.p1Second.id),
        )
      assertCommandFailure(r)

      val r2 = GameStateValidator()(deadGameState)
        .validateAbilityUseOnCoordinates(
          s.characters.p0First.owner(s.gameState).id,
          abilityId,
          s.spawnCoordinates.p0Second,
          UseData(s.characters.p1Second.id),
        )
      assertCommandFailure(r2)
    }

    "not be able to resurrect on foreign spawn" in {
      val deadGameState = s.gameState
        .damageCharacter(s.characters.p0Second.id, Damage(DamageType.True, 99999))(random, s.gameState.id)
        .removeCharacterFromMap(s.characters.p1First.id)(random, s.gameState.id)

      val r = GameStateValidator()(deadGameState)
        .validateAbilityUseOnCoordinates(
          s.characters.p0First.owner(s.gameState).id,
          abilityId,
          s.spawnCoordinates.p1First,
          UseData(s.characters.p0Second.id),
        )
      assertCommandFailure(r)
    }
    "not be able to resurrect on tiles that are not free to stand" in {
      val deadGameState = s.gameState.damageCharacter(s.characters.p0Second.id, Damage(DamageType.True, 99999))(random, s.gameState.id)

      val r = GameStateValidator()(deadGameState)
        .validateAbilityUseOnCoordinates(
          s.characters.p0First.owner(s.gameState).id,
          abilityId,
          s.spawnCoordinates.p0First,
          UseData(s.characters.p0Second.id),
        )
      assertCommandFailure(r)
    }

    "not be able to resurrect characters that are alive" in {
      val aliveGameState = s.gameState.removeCharacterFromMap(s.characters.p0Second.id)(random, s.gameState.id)

      val r = GameStateValidator()(aliveGameState)
        .validateAbilityUseOnCoordinates(
          s.characters.p0First.owner(s.gameState).id,
          abilityId,
          s.spawnCoordinates.p0Second,
          UseData(s.characters.p0Second.id),
        )
      assertCommandFailure(r)
    }
  }

}