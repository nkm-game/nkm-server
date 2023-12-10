package unit

import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.abilities.akame.LittleWarHorn
import com.tosware.nkm.models.game.abilities.sinon.TacticalEscape
import com.tosware.nkm.models.game.character.{CharacterMetadata, StatType}
import com.tosware.nkm.models.game.event.GameEvent
import com.tosware.nkm.models.game.event.GameEvent.{PlayerLost, PlayerWon}
import com.tosware.nkm.models.game.hex.TestHexMapName
import com.tosware.nkm.{CoordinateSeq, randomUUID}
import helpers.*
import org.scalatest.Assertion

import scala.reflect.ClassTag

class GameStateSpec extends TestUtils {
  private val metadata = CharacterMetadata.empty()
    .copy(
      initialSpeed = 3,
      initialBasicAttackRange = 2,
      initialAbilitiesMetadataIds = Seq(LittleWarHorn.metadata.id, TacticalEscape.metadata.id),
    )

  private val s = TestScenario.generate(TestHexMapName.Simple1v1, metadata)
  private val littleWarHornAbilityId = s.defaultAbilityId
  private val tacticalEscapeAbilityId = s.defaultCharacter.state.abilities(1).id

  private val s2v2 = TestScenario.generate(TestHexMapName.Simple2v2, metadata)
  private val s2v2v2 = TestScenario.generate(TestHexMapName.Simple2v2v2, metadata)

  "GameState" should {
    "start abilities with cooldown 0" in {
      s.gameState.abilityStates.values.map(_.cooldown).toSet should be(Set(0))
    }

    "put used ability on cooldown" in {
      val abilityId = littleWarHornAbilityId
      val abilityUsedGameState = s.gameState.useAbility(abilityId)
      abilityUsedGameState.abilityById(abilityId).state(abilityUsedGameState).cooldown should be > 0
    }

    "decrement ability cooldowns at end of characters turn" in {
      val abilityId = littleWarHornAbilityId
      val abilityUsedGameState = s.gameState.useAbility(abilityId)
      val endTurnGameState = abilityUsedGameState.endTurn()

      val oldCooldown = abilityUsedGameState.abilityStates(abilityId).cooldown
      val newCooldown = endTurnGameState.abilityStates(abilityId).cooldown
      oldCooldown should be(newCooldown + 1)
    }

    "decrement effect cooldowns at end of characters turn" in {
      val abilityId = littleWarHornAbilityId
      val abilityUsedGameState = s.gameState.useAbility(abilityId)
      val endTurnGameState = abilityUsedGameState.endTurn()

      val oldCooldown = abilityUsedGameState.characterEffectStates.values.head.cooldown
      val newCooldown = endTurnGameState.characterEffectStates.values.head.cooldown
      oldCooldown should be(newCooldown + 1)
    }

    "remove effects from characters with expired cooldowns" in {
      val abilityId = tacticalEscapeAbilityId // effect with cooldown == 1
      val abilityUsedGameState = s.gameState.useAbility(abilityId)
      val endTurnGameState = abilityUsedGameState.endTurn()

      abilityUsedGameState.effects.size should be(1)
      endTurnGameState.effects.size should be(0)
    }

    "end the game when all players are knocked out" in {
      val p0CharacterKilledGameState = s.gameState.executeCharacter(s.defaultCharacter.id)(random, "test")
      val p1CharacterKilledGameState = s.gameState.executeCharacter(s.defaultEnemy.id)(random, "test")

      p0CharacterKilledGameState.gameStatus should be(GameStatus.Finished)
      p1CharacterKilledGameState.gameStatus should be(GameStatus.Finished)

      p0CharacterKilledGameState.players(1).victoryStatus should be(VictoryStatus.Won)
      p1CharacterKilledGameState.players(0).victoryStatus should be(VictoryStatus.Won)

      p0CharacterKilledGameState.gameLog.events.ofType[PlayerLost].size should be(1)
      p0CharacterKilledGameState.gameLog.events.ofType[PlayerWon].size should be(1)
    }

    "end the game in draw when players are knocked out at the same time" in {
//      TODO(NKM-293): This is possible only in theory, in practice we need simultanous damage
//      val gs: GameState = gameState.executeCharacters(Set(s.defaultCharacter.id, s.defaultEnemy.id))(random, "test")
//
//      gs.gameStatus should be(GameStatus.Finished)
//
//      gs.players.map(_.victoryStatus).toSet should be(Set(VictoryStatus.Drawn))
//      gs.gameLog.events.ofType[PlayerDrew].size should be(2)
    }

    "skip player turn when he has no characters to take action" in {
      val gs = s2v2.gameState
        .executeCharacter(s2v2.p(0)(1).character.id)(random, "test")
        .passTurn(s2v2.p(0)(0).character.id)
        .passTurn(s2v2.p(1)(0).character.id)
      gs.currentPlayer.id should be(s2v2.p(1)(0).ownerId)
    }

    "finish the phase when there are no characters to take action" in {
      val gs = s2v2.gameState
        .executeCharacter(s2v2.p(1)(1).character.id)(random, "test")
        .passTurn(s2v2.p(0)(0).character.id)
        .passTurn(s2v2.p(1)(0).character.id)
        .passTurn(s2v2.p(0)(1).character.id)
      gs.characterIdsThatTookActionThisPhase should be(Set.empty)
      gs.phase.number should be(1)
    }

    "handle knocking out one player correctly" in {
      val gs = s2v2v2.gameState
        .executeCharacter(s2v2v2.p(0)(0).character.id)
        .executeCharacter(s2v2v2.p(0)(1).character.id)

      gs.currentPlayer.id should be(s2v2v2.owners(1))

      val gs2 = s2v2v2.gameState
        .executeCharacter(s2v2v2.p(1)(0).character.id)
        .executeCharacter(s2v2v2.p(1)(1).character.id)

      gs2.currentPlayer.id should be(s2v2v2.owners(0))
      gs2.passTurn(s2v2v2.p(0)(0).character.id).currentPlayer.id should be(s2v2v2.owners(2))
    }

    "handle surrender of one player correctly" in {
      val s = s2v2v2
      val gs = s.gameState.surrender(s.owners(0))

      gs.currentPlayer.id should be(s.owners(1))
//      gs.characterIdsOutsideMap should contain (s.defaultCharacter.id)
//      gs.characterIdsOutsideMap should contain (s.p(0)(1).character.id)

      val gs2 = s.gameState.surrender(s.owners(1))

      gs2.currentPlayer.id should be(s.owners(0))
//      gs.characterIdsOutsideMap should contain (s.defaultEnemy.id)
//      gs.characterIdsOutsideMap should contain (s.p(1)(1).character.id)
      gs2.passTurn(s.defaultCharacter.id).currentPlayer.id should be(s.owners(2))
    }

    "pause the clock on game end" in {
      val p0CharacterKilledGameState = s.gameState.executeCharacter(s.defaultCharacter.id)(random, "test")
      val p1CharacterKilledGameState = s.gameState.executeCharacter(s.defaultEnemy.id)(random, "test")

      p0CharacterKilledGameState.clock.isRunning should be(false)
      p1CharacterKilledGameState.clock.isRunning should be(false)
    }

    "hide teleport events for a basic move" in {
      val bigS = TestScenario.generate(TestHexMapName.Spacey1v1)
      val basicMoveGs = bigS.gameState.basicMoveCharacter(
        bigS.defaultCharacter.id,
        CoordinateSeq((0, 0), (-1, 0), (-2, 0), (-3, 0)),
      )

      basicMoveGs
        .toView(Some(s.owners(0)))
        .gameLog
        .events
        .ofType[GameEvent.CharacterTeleported] should be(empty)

      basicMoveGs
        .toView(None)
        .gameLog
        .events
        .ofType[GameEvent.CharacterTeleported] should be(empty)
    }

    "hide character related events for invisible characters" in {
      val bigS = TestScenario.generate(TestHexMapName.Spacey1v1)

      def sizeOfEventsOfType[T: ClassTag](gs: GameState, playerNumber: Int): Int =
        gs
          .toView(Some(bigS.owners(playerNumber)))
          .gameLog
          .events
          .ofType[T]
          .size

      def assertEventHiddenOfType[T: ClassTag](gs: GameState): Assertion = {
        val s0 = sizeOfEventsOfType[T](gs, 0)
        val s1 = sizeOfEventsOfType[T](gs, 1)

        s1 should be(s0 - 1)
      }

      def assertEventNotHiddenOfType[T: ClassTag](gs: GameState): Assertion = {
        val s0 = sizeOfEventsOfType[T](gs, 0)
        val s1 = sizeOfEventsOfType[T](gs, 1)

        s1 should be(s0)
      }

      val invisibilityEffectUuid = randomUUID()

      val invisibleGs = bigS.gameState.addEffect(
        bigS.defaultCharacter.id,
        effects.Invisibility(invisibilityEffectUuid, 999),
      )

      val effectUuid = randomUUID()
      val effectAddedGs = invisibleGs.addEffect(
        bigS.defaultCharacter.id,
        effects.HealOverTime(effectUuid, 5, 5),
      )

      assertEventNotHiddenOfType[GameEvent.CharacterWentInvisible](effectAddedGs)
      assertEventHiddenOfType[GameEvent.EffectVariableSet](effectAddedGs)

      val effectRemovedGs = effectAddedGs.removeEffect(effectUuid)

      assertEventHiddenOfType[GameEvent.EffectRemovedFromCharacter](effectRemovedGs)

      val basicMoveGs = invisibleGs.basicMoveCharacter(
        bigS.defaultCharacter.id,
        CoordinateSeq((0, 0), (-1, 0), (-2, 0), (-3, 0)),
      )

      assertEventHiddenOfType[GameEvent.CharacterBasicMoved](basicMoveGs)

      // basic attack reveals invisibility
      val basicAttackGs = invisibleGs.basicAttack(
        bigS.defaultCharacter.id,
        bigS.defaultEnemy.id,
      )

      assertEventNotHiddenOfType[GameEvent.CharacterPreparedToAttack](basicAttackGs)
      assertEventNotHiddenOfType[GameEvent.CharacterBasicAttacked](basicAttackGs)

      assertEventNotHiddenOfType[GameEvent.DamageSent](basicAttackGs)
      assertEventNotHiddenOfType[GameEvent.ShieldDamaged](basicAttackGs)
      assertEventNotHiddenOfType[GameEvent.CharacterDamaged](basicAttackGs)

      val damagedGs = invisibleGs.damageCharacter(
        bigS.defaultCharacter.id,
        Damage(DamageType.True, 10),
      )
      assertEventHiddenOfType[GameEvent.CharacterDamaged](damagedGs)

      val healGs = damagedGs.heal(
        bigS.defaultCharacter.id,
        10,
      )
      assertEventHiddenOfType[GameEvent.CharacterHealed](healGs)

      val hpSetGs = invisibleGs.setHp(
        bigS.defaultCharacter.id,
        10,
      )
      assertEventHiddenOfType[GameEvent.CharacterHpSet](hpSetGs)

      val shieldSetGs = invisibleGs.setShield(
        bigS.defaultCharacter.id,
        10,
      )
      assertEventHiddenOfType[GameEvent.CharacterShieldSet](shieldSetGs)

      val statSetGs = invisibleGs.setStat(
        bigS.defaultCharacter.id,
        StatType.Speed,
        100,
      )
      assertEventHiddenOfType[GameEvent.CharacterStatSet](statSetGs)

      val removedFromMapGs = invisibleGs.removeCharacterFromMap(bigS.defaultCharacter.id)
      assertEventHiddenOfType[GameEvent.CharacterRemovedFromMap](removedFromMapGs)

      val revealedGs = removedFromMapGs.removeEffect(invisibilityEffectUuid)
      assertEventNotHiddenOfType[GameEvent.CharacterRemovedFromMap](revealedGs)

      // TODO: test for BasicAttackRefreshed and BasicMoveRefreshed
    }
  }
  "not send shield damaged event if there is no shield" in {
    val damagedGs = s.gameState.damageCharacter(s.defaultCharacter.id, Damage(DamageType.True, 1))
    damagedGs.gameLog.events.ofType[GameEvent.ShieldDamaged] should be(empty)
    damagedGs.gameLog.events.ofType[GameEvent.CharacterDamaged].head.damageAmount should be(1)
  }
  "send shield damaged event if there is shield" in {
    val damagedGs = s.gameState
      .setShield(s.defaultCharacter.id, 1)
      .damageCharacter(s.defaultCharacter.id, Damage(DamageType.True, 1))

    damagedGs.gameLog.events.ofType[GameEvent.ShieldDamaged].head.damageAmount should be(1)
    damagedGs.gameLog.events.ofType[GameEvent.CharacterDamaged] should be(empty)
  }
  "send shield damaged event and not character damaged event if damage exceeds non-zero shield" in {
    val damagedGs = s.gameState
      .setShield(s.defaultCharacter.id, 1)
      .damageCharacter(s.defaultCharacter.id, Damage(DamageType.True, 2))

    damagedGs.gameLog.events.ofType[GameEvent.ShieldDamaged].head.damageAmount should be(1)
    damagedGs.gameLog.events.ofType[GameEvent.CharacterDamaged].head.damageAmount should be(1)
  }
}
