package unit.abilities.dekomori_sanae

import com.tosware.nkm._
import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.abilities.dekomori_sanae.MjolnirHammer
import com.tosware.nkm.models.game.ability.UseData
import com.tosware.nkm.models.game.event.GameEvent.CharacterDamaged
import com.tosware.nkm.models.game.hex.{HexCoordinates, TestHexMapName}
import helpers.{TestScenario, TestUtils}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import spray.json._

class MjolnirHammerSpec
  extends AnyWordSpecLike
    with Matchers
    with TestUtils
{

  private val abilityMetadata = MjolnirHammer.metadata
  private val s = TestScenario.generate(TestHexMapName.Simple1v9Line, abilityMetadata.id)

  private val noneUseData =
    UseData(Seq.empty[HexCoordinates].toJson.toString)

  private val singleUseData =
    UseData(Seq(s.p(1)(0).spawnCoordinates).toJson.toString)

  private val doubleUseData =
    UseData((0 to 1).map(x => s.p(1)(x).spawnCoordinates).toJson.toString)

  private val tripleUseData =
    UseData((0 to 2).map(x => s.p(1)(x).spawnCoordinates).toJson.toString)

  private val usedOnSingleGs: GameState =
    s.gameState
      .useAbility(s.defaultAbilityId, singleUseData)

  private val usedOnDoubleGs: GameState =
    s.gameState
      .useAbility(s.defaultAbilityId, doubleUseData)


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

    "apply half damage on second hit if used on the same target" in {
      val dmgAmounts =
        usedOnSingleGs
          .gameLog
          .events
          .ofType[CharacterDamaged]
          .causedBy(s.defaultAbilityId)
          .map(_.damage.amount)

      dmgAmounts.size should be (2)
      dmgAmounts(0) should be (dmgAmounts(1) * 2)
    }


    "apply the same damage if used on two targets" in {
      val dmgAmounts =
        usedOnDoubleGs
          .gameLog
          .events
          .ofType[CharacterDamaged]
          .causedBy(s.defaultAbilityId)
          .map(_.damage.amount)

      dmgAmounts.size should be (2)
      dmgAmounts(0) should be (dmgAmounts(1) * 2)
    }
  }
}