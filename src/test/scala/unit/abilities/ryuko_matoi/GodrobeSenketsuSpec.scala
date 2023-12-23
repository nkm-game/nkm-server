package unit.abilities.ryuko_matoi

import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.abilities.ryuko_matoi.GodrobeSenketsu
import com.tosware.nkm.models.game.hex.TestHexMapName
import helpers.{TestScenario, TestUtils}

class GodrobeSenketsuSpec extends TestUtils {
  private val abilityMetadata = GodrobeSenketsu.metadata
  private val s = TestScenario.generate(TestHexMapName.Simple1v1, abilityMetadata.id)
  private val gameState: GameState = s.ultGs
  private val abilityId = s.defaultAbilityId

  abilityMetadata.name must {
    "be able to use" in {
      assertCommandSuccess {
        GameStateValidator()(gameState).validateAbilityUse(s.owners(0), abilityId)
      }
    }

    "apply flying effect" in {
      val abilityUsedGameState: GameState = gameState.useAbility(abilityId)
      abilityUsedGameState.characterById(s.defaultCharacter.id).state.effects.ofType[effects.Fly].size should be > 0
    }

    "be able to incrementally increase damage while receiving damage" in {
      val oldAD = s.defaultCharacter.state.attackPoints
      val oldHP = s.defaultCharacter.state.healthPoints

      val abilityUsedGameState: GameState = gameState.useAbility(abilityId)
      val newAD1 = abilityUsedGameState.characterById(s.defaultCharacter.id).state.attackPoints
      val newHP1 = abilityUsedGameState.characterById(s.defaultCharacter.id).state.healthPoints

      oldAD should be < newAD1
      oldHP should be(newHP1)

      val oneTurnPassedGameState: GameState = abilityUsedGameState
        .endTurn()
        .passTurn(s.defaultEnemy.id)
      val newAD2 = oneTurnPassedGameState.characterById(s.defaultCharacter.id).state.attackPoints
      val newHP2 = oneTurnPassedGameState.characterById(s.defaultCharacter.id).state.healthPoints

      newAD1 should be < newAD2
      newHP1 should be > newHP2

      val twoTurnsPassedGameState: GameState = oneTurnPassedGameState
        .passTurn(s.defaultCharacter.id)
        .passTurn(s.defaultEnemy.id)
      val newAD3 = twoTurnsPassedGameState.characterById(s.defaultCharacter.id).state.attackPoints
      val newHP3 = twoTurnsPassedGameState.characterById(s.defaultCharacter.id).state.healthPoints

      newAD2 should be < newAD3
      newHP2 should be > newHP3
    }

    "be able to enable and disable effect" in {
      val oldAD = s.defaultCharacter.state.attackPoints
      val abilityUsedGameState: GameState = gameState.useAbility(abilityId)

      abilityUsedGameState.abilityStates(abilityId).isEnabled should be(true)
      abilityUsedGameState.abilityStates(abilityId).cooldown should be(0)

      assertCommandFailure {
        GameStateValidator()(abilityUsedGameState).validateAbilityUse(s.owners(0), abilityId)
      }
      assertCommandSuccess {
        val gs = abilityUsedGameState
          .endTurn()
          .passTurn(s.defaultEnemy.id)
        GameStateValidator()(gs).validateAbilityUse(s.owners(0), abilityId)
      }

      val abilityDisabledGameState: GameState = abilityUsedGameState
        .endTurn()
        .passTurn(s.defaultEnemy.id)
        .useAbility(abilityId)

      abilityDisabledGameState.characterById(s.defaultCharacter.id).state.effects.ofType[effects.Fly].size should be(0)

      val newAD = abilityDisabledGameState.characterById(s.defaultCharacter.id).state.attackPoints
      oldAD should be(newAD)
      abilityDisabledGameState.abilityStates(abilityId).isEnabled should be(false)
      abilityDisabledGameState.abilityStates(abilityId).cooldown should be(abilityMetadata.variables("cooldown"))
    }
  }
}
