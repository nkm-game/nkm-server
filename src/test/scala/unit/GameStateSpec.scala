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
  private implicit val gameState: GameState = s.gameState
  private val littleWarHornAbilityId = s.characters.p0.state.abilities(0).id
  private val tacticalEscapeAbilityId = s.characters.p0.state.abilities(1).id

  "GameState" must {
    "start abilities with cooldown 0" in {
      gameState.abilities.map(_.state.cooldown) should be (Set(0))
    }
    "put used ability on cooldown" in {
      val abilityId = littleWarHornAbilityId
      val abilityUsedGameState = gameState.useAbilityWithoutTarget(abilityId)
      abilityUsedGameState.abilityById(abilityId).get.state.cooldown should be > 0
    }
    "decrement ability cooldowns at end of characters turn" in {
      val abilityId = littleWarHornAbilityId
      val abilityUsedGameState = gameState.useAbilityWithoutTarget(abilityId)
      val endTurnGameState = abilityUsedGameState.endTurn()

      val oldCooldown = abilityUsedGameState.abilityById(abilityId).get.state.cooldown
      val newCooldown = endTurnGameState.abilityById(abilityId).get.state.cooldown
      oldCooldown should be (newCooldown + 1)
    }
    "decrement effect cooldowns at end of characters turn" in {
      val abilityId = littleWarHornAbilityId
      val abilityUsedGameState = gameState.useAbilityWithoutTarget(abilityId)
      val endTurnGameState = abilityUsedGameState.endTurn()

      val oldCooldown = abilityUsedGameState.effects.head.cooldown
      val newCooldown = endTurnGameState.effects.head.cooldown
      oldCooldown should be (newCooldown + 1)
    }
    "remove effects from characters with expired cooldowns" in {
      val abilityId = tacticalEscapeAbilityId // effect with cooldown == 1
      val abilityUsedGameState = gameState.useAbilityWithoutTarget(abilityId)
      val endTurnGameState = abilityUsedGameState.endTurn()

      abilityUsedGameState.effects.size should be (1)
      endTurnGameState.effects.size should be (0)
    }
  }
}
