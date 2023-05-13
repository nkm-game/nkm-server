package unit

import com.tosware.nkm.CoordinateSeq
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.abilities.akame.LittleWarHorn
import com.tosware.nkm.models.game.abilities.sinon.TacticalEscape
import com.tosware.nkm.models.game.character.CharacterMetadata
import com.tosware.nkm.models.game.event.GameEvent
import com.tosware.nkm.models.game.hex.TestHexMapName
import helpers.{TestScenario, TestUtils, scenarios}
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
  private val littleWarHornAbilityId = s.p(0)(0).character.state.abilities(0).id
  private val tacticalEscapeAbilityId = s.p(0)(0).character.state.abilities(1).id

  "GameState" must {
    "start abilities with cooldown 0" in {
      gameState.abilityStates.values.map(_.cooldown).toSet should be (Set(0))
    }

    "put used ability on cooldown" in {
      val abilityId = littleWarHornAbilityId
      val abilityUsedGameState = gameState.useAbility(abilityId)
      abilityUsedGameState.abilityById(abilityId).state(abilityUsedGameState).cooldown should be > 0
    }

    "decrement ability cooldowns at end of characters turn" in {
      val abilityId = littleWarHornAbilityId
      val abilityUsedGameState = gameState.useAbility(abilityId)
      val endTurnGameState = abilityUsedGameState.endTurn()

      val oldCooldown = abilityUsedGameState.abilityStates(abilityId).cooldown
      val newCooldown = endTurnGameState.abilityStates(abilityId).cooldown
      oldCooldown should be (newCooldown + 1)
    }

    "decrement effect cooldowns at end of characters turn" in {
      val abilityId = littleWarHornAbilityId
      val abilityUsedGameState = gameState.useAbility(abilityId)
      val endTurnGameState = abilityUsedGameState.endTurn()

      val oldCooldown = abilityUsedGameState.characterEffectStates.values.head.cooldown
      val newCooldown = endTurnGameState.characterEffectStates.values.head.cooldown
      oldCooldown should be (newCooldown + 1)
    }

    "remove effects from characters with expired cooldowns" in {
      val abilityId = tacticalEscapeAbilityId // effect with cooldown == 1
      val abilityUsedGameState = gameState.useAbility(abilityId)
      val endTurnGameState = abilityUsedGameState.endTurn()

      abilityUsedGameState.effects.size should be (1)
      endTurnGameState.effects.size should be (0)
    }

    "end the game when all players are knocked out" in {
      val p0CharacterKilledGameState = gameState.executeCharacter(s.p(0)(0).character.id)(random, "test")
      val p1CharacterKilledGameState = gameState.executeCharacter(s.p(1)(0).character.id)(random, "test")

      p0CharacterKilledGameState.gameStatus should be (GameStatus.Finished)
      p1CharacterKilledGameState.gameStatus should be (GameStatus.Finished)

      p0CharacterKilledGameState.players(1).victoryStatus should be (VictoryStatus.Won)
      p1CharacterKilledGameState.players(0).victoryStatus should be (VictoryStatus.Won)
    }

    "skip player turn when he has no characters to take action" in {
      val simple2v2Scenario = scenarios.Simple2v2TestScenario(metadata)
      val gs = simple2v2Scenario.gameState
        .executeCharacter(simple2v2Scenario.p(0)(1).character.id)(random, "test")
        .passTurn(simple2v2Scenario.p(0)(0).character.id)
        .passTurn(simple2v2Scenario.p(1)(0).character.id)
      gs.currentPlayer.id should be (simple2v2Scenario.p(1)(0).ownerId)
    }

    "finish the phase when there are no characters to take action" in {
      val simple2v2Scenario = scenarios.Simple2v2TestScenario(metadata)
      val gs = simple2v2Scenario.gameState
        .executeCharacter(simple2v2Scenario.p(1)(1).character.id)(random, "test")
        .passTurn(simple2v2Scenario.p(0)(0).character.id)
        .passTurn(simple2v2Scenario.p(1)(0).character.id)
        .passTurn(simple2v2Scenario.p(0)(1).character.id)
      gs.characterIdsThatTookActionThisPhase should be (Set.empty)
      gs.phase.number should be (1)
    }

    "handle knocking out one player correctly" in {
      val s = scenarios.Simple2v2v2TestScenario(metadata)
      val gs = s.gameState
        .executeCharacter(s.p(0)(0).character.id)
        .executeCharacter(s.p(0)(1).character.id)

      gs.currentPlayer.id should be (s.p(1)(0).ownerId)

      val gs2 = s.gameState
        .executeCharacter(s.p(1)(0).character.id)
        .executeCharacter(s.p(1)(1).character.id)

      gs2.currentPlayer.id should be (s.p(0)(0).ownerId)
      gs2.passTurn(s.p(0)(0).character.id).currentPlayer.id should be (s.p(2)(0).ownerId)
    }

    "handle surrender of one player correctly" in {
      val s = scenarios.Simple2v2v2TestScenario(metadata)
      val gs = s.gameState
        .surrender(s.p(0)(0).ownerId)

      gs.currentPlayer.id should be (s.p(1)(0).ownerId)
//      gs.characterIdsOutsideMap should contain (s.p(0)(0).character.id)
//      gs.characterIdsOutsideMap should contain (s.p(0)(1).character.id)

      val gs2 = s.gameState
        .surrender(s.p(1)(0).ownerId)

      gs2.currentPlayer.id should be (s.p(0)(0).ownerId)
//      gs.characterIdsOutsideMap should contain (s.p(1)(0).character.id)
//      gs.characterIdsOutsideMap should contain (s.p(1)(1).character.id)
      gs2.passTurn(s.p(0)(0).character.id).currentPlayer.id should be (s.p(2)(0).ownerId)
    }

    "pause the clock on game end" in {
      val p0CharacterKilledGameState = gameState.executeCharacter(s.p(0)(0).character.id)(random, "test")
      val p1CharacterKilledGameState = gameState.executeCharacter(s.p(1)(0).character.id)(random, "test")

      p0CharacterKilledGameState.clock.isRunning should be (false)
      p1CharacterKilledGameState.clock.isRunning should be (false)
    }

    "hide teleport events for a basic move" in {
      val bigS = TestScenario.generate(TestHexMapName.Spacey1v1, CharacterMetadata.empty())
      val basicMoveGs = bigS.gameState.basicMoveCharacter(
        bigS.defaultCharacter.id,
        CoordinateSeq((0, 0), (-1, 0), (-2, 0), (-3, 0))
      )

      basicMoveGs
        .toView(Some(s.owners(0)))
        .gameLog
        .events
        .ofType[GameEvent.CharacterTeleported]
        .size should be(0)

      basicMoveGs
        .toView(None)
        .gameLog
        .events
        .ofType[GameEvent.CharacterTeleported]
        .size should be(0)
    }
  }
}
