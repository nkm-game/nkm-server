package unit.abilities.monkey_d_luffy

import com.tosware.nkm.*
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.abilities.monkey_d_luffy.RubberRubberFruit
import com.tosware.nkm.models.game.ability.{AbilityType, UseData}
import com.tosware.nkm.models.game.event.GameEvent
import com.tosware.nkm.models.game.hex.{HexCoordinates, TestHexMapName}
import helpers.{TestScenario, TestUtils}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class RubberRubberFruitSpec
    extends AnyWordSpecLike
    with Matchers
    with TestUtils {
  private val abilityMetadata = RubberRubberFruit.metadata
  private val s = TestScenario.generate(TestHexMapName.RubberRubberFruit, abilityMetadata.id)

  private val rocket1Gs = s.gameState.useAbility(s.defaultAbilityId, UseData(HexCoordinates(-6, 0)))
  private val rocket2Gs = s.gameState.useAbility(s.defaultAbilityId, UseData(HexCoordinates(-4, 0)))
  private val rocket3Gs = s.gameState.useAbility(s.defaultAbilityId, UseData(HexCoordinates(-2, 0)))
  private val bazookaGs = s.gameState.useAbility(s.defaultAbilityId, UseData(HexCoordinates(1, 0)))
  private val pistolGs = s.gameState.useAbility(s.defaultAbilityId, UseData(HexCoordinates(8, 0)))

  private val enchantedGs = s.gameState
    .addEffect(
      s.defaultCharacter.id,
      effects.AbilityEnchant(randomUUID(), 2, AbilityType.Normal),
    )

  private val jetBazookaGs = enchantedGs.useAbility(s.defaultAbilityId, UseData(HexCoordinates(1, 0)))
  private val jetPistolGs = enchantedGs.useAbility(s.defaultAbilityId, UseData(HexCoordinates(8, 0)))

  abilityMetadata.name must {
    "jump with rocket" in {
      s.defaultCharacter.parentCellOpt(rocket1Gs).get.coordinates.toTuple shouldBe (-5, 0)
      s.defaultCharacter.parentCellOpt(rocket2Gs).get.coordinates.toTuple shouldBe (-5, 0)
      s.defaultCharacter.parentCellOpt(rocket3Gs).get.coordinates.toTuple shouldBe (-3, 0)
    }

    "knockback with bazooka" in {
      s.defaultEnemy.parentCellOpt(bazookaGs).get.coordinates.toTuple shouldBe (9, 0)
    }

    "knockback further with jet bazooka" in {
      s.defaultEnemy.parentCellOpt(jetBazookaGs).get.coordinates.toTuple shouldBe (13, 0)
    }

    "damage with pistols and bazookas" in {
      def assertOneDamaged(gs: GameState) =
        gs.gameLog.events
          .ofType[GameEvent.CharacterDamaged]
          .causedBy(s.defaultAbilityId)
          .size should be(1)

      assertOneDamaged(bazookaGs)
      assertOneDamaged(pistolGs)
      assertOneDamaged(jetBazookaGs)
      assertOneDamaged(jetPistolGs)
    }

    "not crash when there is no space to move" in {
      val closeToWallGs = s.gameState.teleportCharacter(s.defaultCharacter.id, HexCoordinates(0, 100))
      closeToWallGs.useAbility(s.defaultAbilityId, UseData(HexCoordinates(1, 100)))
    }
  }
}
