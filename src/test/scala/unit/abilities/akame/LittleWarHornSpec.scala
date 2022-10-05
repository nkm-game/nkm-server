package unit.abilities.akame

import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.abilities.akame.LittleWarHorn
import com.tosware.nkm.models.game.effects.StatBuff
import com.tosware.nkm.models.game.hex.HexUtils._
import helpers.{TestUtils, scenarios}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import scala.annotation.tailrec

class LittleWarHornSpec
  extends AnyWordSpecLike
    with Matchers
    with TestUtils
{
  private val abilityMetadata = LittleWarHorn.metadata
  private val characterMetadata = CharacterMetadata.empty()
    .copy(
      initialAbilitiesMetadataIds = Seq(abilityMetadata.id),
      initialSpeed = 7
    )
  private val s = scenarios.Simple1v1TestScenario(characterMetadata)
  private implicit val gameState: GameState = s.gameState
  private val abilityId = s.characters.p0.state.abilities.head.id

  abilityMetadata.name must {
    "be able to use" in {
      val r = GameStateValidator()
        .validateAbilityUseWithoutTarget(
          s.characters.p0.owner.id,
          abilityId,
        )
      assertCommandSuccess(r)
    }
    "add AD and speed buffs" in {
      val newGameState: GameState = gameState.useAbilityWithoutTarget(abilityId)
      val statBuffs = newGameState
        .characterById(s.characters.p0.id).get
        .state
        .effects
        .ofType[StatBuff]
      statBuffs.count(b => b.statType == StatType.AttackPoints) should be (1)
      statBuffs.count(b => b.statType == StatType.Speed) should be (1)
    }

    def skipPhase(gameState: GameState): GameState =
      gameState
        .passTurn(s.characters.p0.id)
        .passTurn(s.characters.p1.id)

    @tailrec
    def skipPhaseN(n: Int)(gameState: GameState): GameState =
      if(n <= 0) gameState
      else skipPhaseN(n-1)(skipPhase(gameState))

    "set characters base speed after duration time" in {
      val duration = abilityMetadata.variables("duration")
      val initialSpeed = s.characters.p0.state.pureSpeed

      val abilityUseGameState: GameState = gameState.useAbilityWithoutTarget(abilityId)
      abilityUseGameState
        .characterById(s.characters.p0.id).get
        .state.pureSpeed should be (initialSpeed)

      val afterDurationGameState = skipPhaseN(duration)(abilityUseGameState)
      afterDurationGameState
        .characterById(s.characters.p0.id).get
        .state.pureSpeed should not be initialSpeed
    }
  }
}