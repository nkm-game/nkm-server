package unit.abilities.dekomori_sanae

import com.tosware.nkm.*
import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game.abilities.dekomori_sanae.MjolnirHammer
import com.tosware.nkm.models.game.ability.UseData
import com.tosware.nkm.models.game.event.GameEvent.DamageSent
import com.tosware.nkm.models.game.game_state.GameState
import com.tosware.nkm.models.game.hex.TestHexMapName
import helpers.{TestScenario, TestUtils}

class MjolnirHammerSpec extends TestUtils {

  private val abilityMetadata = MjolnirHammer.metadata
  private val s = TestScenario.generate(TestHexMapName.Simple1v9Line, abilityMetadata.id)

  private val noneUseData =
    UseData()

  private val singleUseData =
    UseData(s.defaultEnemy.id)

  private val doubleUseData =
    UseData((0 to 1).map(x => s.p(1)(x).character.id))

  private val tripleUseData =
    UseData((0 to 2).map(x => s.p(1)(x).character.id))

  private val usedOnSingleGs: GameState =
    s.gameState
      .useAbility(s.defaultAbilityId, singleUseData)

  private val usedOnDoubleGs: GameState =
    s.gameState
      .useAbility(s.defaultAbilityId, doubleUseData)

  private val doubleInvalidUseData =
    UseData(Seq(s.defaultEnemy.id, s.defaultCharacter.id))

  abilityMetadata.name must {
    "not be able to use without target" in {
      assertCommandFailure {
        GameStateValidator()(s.gameState)
          .validateAbilityUse(s.owners(0), s.defaultAbilityId, noneUseData)
      }
    }
    "be able to use on single target" in {
      assertCommandSuccess {
        GameStateValidator()(s.gameState)
          .validateAbilityUse(s.owners(0), s.defaultAbilityId, singleUseData)
      }
    }

    "be able to use on double targets" in {
      assertCommandSuccess {
        GameStateValidator()(s.gameState)
          .validateAbilityUse(s.owners(0), s.defaultAbilityId, doubleUseData)
      }
    }

    "not be able to use on triple targets" in {
      assertCommandFailure {
        GameStateValidator()(s.gameState)
          .validateAbilityUse(s.owners(0), s.defaultAbilityId, tripleUseData)
      }
    }

    "not be able to use on double invalid targets that exist on map" in {
      assertCommandFailure {
        GameStateValidator()(s.gameState)
          .validateAbilityUse(s.owners(0), s.defaultAbilityId, doubleInvalidUseData)
      }
    }

    "send half damage on second hit if used on the same target" in {
      val dmgAmounts =
        usedOnSingleGs
          .gameLog
          .events
          .ofType[DamageSent]
          .causedBy(s.defaultAbilityId)
          .map(_.damage.amount)

      dmgAmounts.size should be(2)
      dmgAmounts(0) should be(dmgAmounts(1) * 2)
    }

    "send the same damage if used on two targets" in {
      val dmgAmounts =
        usedOnDoubleGs
          .gameLog
          .events
          .ofType[DamageSent]
          .causedBy(s.defaultAbilityId)
          .map(_.damage.amount)

      dmgAmounts.size should be(2)
      dmgAmounts(0) should be(dmgAmounts(1))
    }
  }
}
