package unit

import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.abilities.akame.LittleWarHorn
import com.tosware.nkm.models.game.abilities.sinon.TacticalEscape
import helpers.{TestUtils, scenarios}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class GameStateSpec
  extends AnyWordSpecLike
    with Matchers
    with TestUtils
{
  private val metadata = CharacterMetadata.empty()
    .copy(
      initialSpeed = 3,
      initialBasicAttackRange = 2,
      initialAbilitiesMetadataIds = Seq(LittleWarHorn.metadata.id, TacticalEscape.metadata.id)
    )

  private val s = scenarios.Simple1v1TestScenario(metadata)
  private val gameState: GameState = s.gameState
  private val littleWarHornAbilityId = s.characters.p0.state.abilities(0).id
  private val tacticalEscapeAbilityId = s.characters.p0.state.abilities(1).id

  "GameState" must {
    "start abilities with cooldown 0" in {
      gameState.abilityStates.values.map(_.cooldown).toSet should be (Set(0))
    }

    "put used ability on cooldown" in {
      val abilityId = littleWarHornAbilityId
      val abilityUsedGameState = gameState.useAbilityWithoutTarget(abilityId)
      abilityUsedGameState.abilityById(abilityId).state(abilityUsedGameState).cooldown should be > 0
    }

    "decrement ability cooldowns at end of characters turn" in {
      val abilityId = littleWarHornAbilityId
      val abilityUsedGameState = gameState.useAbilityWithoutTarget(abilityId)
      val endTurnGameState = abilityUsedGameState.endTurn()

      val oldCooldown = abilityUsedGameState.abilityStates(abilityId).cooldown
      val newCooldown = endTurnGameState.abilityStates(abilityId).cooldown
      oldCooldown should be (newCooldown + 1)
    }

    "decrement effect cooldowns at end of characters turn" in {
      val abilityId = littleWarHornAbilityId
      val abilityUsedGameState = gameState.useAbilityWithoutTarget(abilityId)
      val endTurnGameState = abilityUsedGameState.endTurn()

      val oldCooldown = abilityUsedGameState.characterEffectStates.values.head.cooldown
      val newCooldown = endTurnGameState.characterEffectStates.values.head.cooldown
      oldCooldown should be (newCooldown + 1)
    }

    "remove effects from characters with expired cooldowns" in {
      val abilityId = tacticalEscapeAbilityId // effect with cooldown == 1
      val abilityUsedGameState = gameState.useAbilityWithoutTarget(abilityId)
      val endTurnGameState = abilityUsedGameState.endTurn()

      abilityUsedGameState.effects.size should be (1)
      endTurnGameState.effects.size should be (0)
    }

    "end the game when all players are knocked out" in {
      val p0CharacterKilledGameState = gameState.executeCharacter(s.characters.p0.id)(random, "test")
      val p1CharacterKilledGameState = gameState.executeCharacter(s.characters.p1.id)(random, "test")

      p0CharacterKilledGameState.gameStatus should be (GameStatus.Finished)
      p1CharacterKilledGameState.gameStatus should be (GameStatus.Finished)

      p0CharacterKilledGameState.players(1).victoryStatus should be (VictoryStatus.Won)
      p1CharacterKilledGameState.players(0).victoryStatus should be (VictoryStatus.Won)
    }

    "skip player turn when he has no characters to take action" in {
      val simple2v2Scenario = scenarios.Simple2v2TestScenario(metadata)
      val gs = simple2v2Scenario.gameState
        .executeCharacter(simple2v2Scenario.characters.p0Second.id)(random, "test")
        .passTurn(simple2v2Scenario.characters.p0First.id)
        .passTurn(simple2v2Scenario.characters.p1First.id)
      gs.currentPlayer.id should be (simple2v2Scenario.characters.p1First.owner(gs).id)
    }

    "finish the phase when there are no characters to take action" in {
      val simple2v2Scenario = scenarios.Simple2v2TestScenario(metadata)
      val gs = simple2v2Scenario.gameState
        .executeCharacter(simple2v2Scenario.characters.p1Second.id)(random, "test")
        .passTurn(simple2v2Scenario.characters.p0First.id)
        .passTurn(simple2v2Scenario.characters.p1First.id)
        .passTurn(simple2v2Scenario.characters.p0Second.id)
      gs.characterIdsThatTookActionThisPhase should be (Set.empty)
      gs.phase.number should be (1)
    }

    "handle knocking out one player correctly" in {
      val s = scenarios.Simple2v2v2TestScenario(metadata)
      val gs = s.gameState
        .executeCharacter(s.characters.p0First.id)
        .executeCharacter(s.characters.p0Second.id)

      gs.currentPlayer.id should be (s.characters.p1First.owner(gs).id)

      val gs2 = s.gameState
        .executeCharacter(s.characters.p1First.id)
        .executeCharacter(s.characters.p1Second.id)

      gs2.currentPlayer.id should be (s.characters.p0First.owner(gs).id)
      gs2.passTurn(s.characters.p0First.id).currentPlayer.id should be (s.characters.p2First.owner(gs).id)
    }

    "handle surrender of one player correctly" in {
      val s = scenarios.Simple2v2v2TestScenario(metadata)
      val gs = s.gameState
        .surrender(s.characters.p0First.owner(s.gameState).id)

      gs.currentPlayer.id should be (s.characters.p1First.owner(gs).id)
//      gs.characterIdsOutsideMap should contain (s.characters.p0First.id)
//      gs.characterIdsOutsideMap should contain (s.characters.p0Second.id)

      val gs2 = s.gameState
        .surrender(s.characters.p1First.owner(s.gameState).id)

      gs2.currentPlayer.id should be (s.characters.p0First.owner(gs).id)
//      gs.characterIdsOutsideMap should contain (s.characters.p1First.id)
//      gs.characterIdsOutsideMap should contain (s.characters.p1Second.id)
      gs2.passTurn(s.characters.p0First.id).currentPlayer.id should be (s.characters.p2First.owner(gs).id)
    }

    "pause the clock on game end" in {
      val p0CharacterKilledGameState = gameState.executeCharacter(s.characters.p0.id)(random, "test")
      val p1CharacterKilledGameState = gameState.executeCharacter(s.characters.p1.id)(random, "test")

      p0CharacterKilledGameState.clock.isRunning should be (false)
      p1CharacterKilledGameState.clock.isRunning should be (false)
    }
  }
}
