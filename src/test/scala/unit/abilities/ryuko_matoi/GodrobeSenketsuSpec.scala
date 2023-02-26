package unit.abilities.ryuko_matoi

import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.abilities.ryuko_matoi.GodrobeSenketsu
import com.tosware.nkm.models.game.character.CharacterMetadata
import helpers.{TestUtils, scenarios}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class GodrobeSenketsuSpec
  extends AnyWordSpecLike
    with Matchers
    with TestUtils
{
  private val abilityMetadata = GodrobeSenketsu.metadata
  private val metadata = CharacterMetadata.empty()
    .copy(initialAbilitiesMetadataIds = Seq(abilityMetadata.id))
  private val s = scenarios.Simple1v1TestScenario(metadata)
  private val gameState: GameState = s.gameState.incrementPhase(4)
  private val abilityId = s.characters.p0.state.abilities.head.id

  abilityMetadata.name must {
    "be able to use" in {
      assertCommandSuccess {
        GameStateValidator()(gameState).validateAbilityUse(s.characters.p0.owner(gameState).id, abilityId)
      }
    }

    "apply flying effect" in {
      val abilityUsedGameState: GameState = gameState.useAbility(abilityId)
      abilityUsedGameState.characterById(s.characters.p0.id).state.effects.ofType[effects.Fly].size should be > 0
    }

    "be able to incrementally increase damage while receiving damage" in {
      val oldAD = s.characters.p0.state.attackPoints
      val oldHP = s.characters.p0.state.healthPoints

      val abilityUsedGameState: GameState = gameState.useAbility(abilityId)
      val newAD1 = abilityUsedGameState.characterById(s.characters.p0.id).state.attackPoints
      val newHP1 = abilityUsedGameState.characterById(s.characters.p0.id).state.healthPoints

      oldAD should be < newAD1
      oldHP should be (newHP1)

      val oneTurnPassedGameState: GameState = abilityUsedGameState
        .endTurn()
        .passTurn(s.characters.p1.id)
      val newAD2 = oneTurnPassedGameState.characterById(s.characters.p0.id).state.attackPoints
      val newHP2 = oneTurnPassedGameState.characterById(s.characters.p0.id).state.healthPoints

      newAD1 should be < newAD2
      newHP1 should be > newHP2

      val twoTurnsPassedGameState: GameState = oneTurnPassedGameState
        .passTurn(s.characters.p0.id)
        .passTurn(s.characters.p1.id)
      val newAD3 = twoTurnsPassedGameState.characterById(s.characters.p0.id).state.attackPoints
      val newHP3 = twoTurnsPassedGameState.characterById(s.characters.p0.id).state.healthPoints

      newAD2 should be < newAD3
      newHP2 should be > newHP3
    }

    "be able to enable and disable effect" in {
      val oldAD = s.characters.p0.state.attackPoints
      val abilityUsedGameState: GameState = gameState.useAbility(abilityId)

      abilityUsedGameState.abilityStates(abilityId).isEnabled should be (true)
      abilityUsedGameState.abilityStates(abilityId).cooldown should be (0)

      assertCommandFailure {
        GameStateValidator()(abilityUsedGameState).validateAbilityUse(s.characters.p0.owner(gameState).id, abilityId)
      }
      assertCommandSuccess {
        val gs = abilityUsedGameState
          .endTurn()
          .passTurn(s.characters.p1.id)
        GameStateValidator()(gs).validateAbilityUse(s.characters.p0.owner(gameState).id, abilityId)
      }

      val abilityDisabledGameState: GameState = abilityUsedGameState
        .endTurn()
        .passTurn(s.characters.p1.id)
        .useAbility(abilityId)

      abilityDisabledGameState.characterById(s.characters.p0.id).state.effects.ofType[effects.Fly].size should be (0)

      val newAD = abilityDisabledGameState.characterById(s.characters.p0.id).state.attackPoints
      oldAD should be (newAD)
      abilityDisabledGameState.abilityStates(abilityId).isEnabled should be (false)
      abilityDisabledGameState.abilityStates(abilityId).cooldown should be (abilityMetadata.variables("cooldown"))
    }
  }
}