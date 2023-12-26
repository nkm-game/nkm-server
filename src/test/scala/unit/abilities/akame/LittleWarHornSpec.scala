package unit.abilities.akame

import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game.abilities.akame.LittleWarHorn
import com.tosware.nkm.models.game.character.{CharacterMetadata, StatType}
import com.tosware.nkm.models.game.effects.StatBuff
import com.tosware.nkm.models.game.game_state.GameState
import com.tosware.nkm.models.game.hex.TestHexMapName
import helpers.{TestScenario, TestUtils}

import scala.annotation.tailrec

class LittleWarHornSpec extends TestUtils {
  private val abilityMetadata = LittleWarHorn.metadata
  private val characterMetadata = CharacterMetadata.empty()
    .copy(
      initialAbilitiesMetadataIds = Seq(abilityMetadata.id),
      initialSpeed = 7,
    )
  private val s = TestScenario.generate(TestHexMapName.Simple1v1, characterMetadata)
  implicit private val gameState: GameState = s.ultGs
  private val abilityId = s.defaultAbilityId

  abilityMetadata.name must {
    "be able to use" in {
      val r = GameStateValidator()
        .validateAbilityUse(
          s.owners(0),
          abilityId,
        )
      assertCommandSuccess(r)
    }
    "add AD and speed buffs" in {
      val newGameState: GameState = gameState.useAbility(abilityId)
      val statBuffs = newGameState
        .characterById(s.defaultCharacter.id)
        .state
        .effects
        .ofType[StatBuff]
      statBuffs.count(b => b.statType == StatType.AttackPoints) should be(1)
      statBuffs.count(b => b.statType == StatType.Speed) should be(1)
    }

    def skipPhase(gameState: GameState): GameState =
      gameState
        .passTurn(s.defaultCharacter.id)
        .passTurn(s.defaultEnemy.id)

    @tailrec
    def skipPhaseN(n: Int)(gameState: GameState): GameState =
      if (n <= 0) gameState
      else skipPhaseN(n - 1)(skipPhase(gameState))

    "set characters base speed after duration time" in {
      val duration = abilityMetadata.variables("duration")
      val initialSpeed = s.defaultCharacter.state.pureSpeed

      val abilityUseGameState: GameState = gameState.useAbility(abilityId)
      abilityUseGameState
        .characterById(s.defaultCharacter.id)
        .state.pureSpeed should be(initialSpeed)

      val afterDurationGameState = skipPhaseN(duration)(abilityUseGameState)
      afterDurationGameState
        .characterById(s.defaultCharacter.id)
        .state.pureSpeed should not be initialSpeed
    }
  }
}
