package unit.abilities.akame

import com.tosware.nkm.models.game.CharacterEffectName._
import com.tosware.nkm.models.game.GameEvent.CharacterDied
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.abilities.akame.{Eliminate, Murasame}
import helpers.{TestUtils, scenarios}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import scala.annotation.tailrec

class MurasameSpec
  extends AnyWordSpecLike
    with Matchers
    with TestUtils
{
  private val abilityMetadata = Murasame.metadata
  private val characterMetadata = CharacterMetadata.empty()
    .copy(initialAbilitiesMetadataIds = Seq(
      abilityMetadata.id,
      Eliminate.metadata.id,
    ))
  private val s = scenarios.Simple1v1TestScenario(characterMetadata)
  private implicit val gameState: GameState = s.gameState
  private val eliminateAbilityId = s.characters.p0.state.abilities.tail.head.id

  abilityMetadata.name must {
    "apply poison on basic attack" in {
      val newGameState: GameState = gameState.basicAttack(
        s.characters.p0.id,
        s.characters.p1.id
      )
      newGameState
        .characterById(s.characters.p1.id)
        .state
        .effects
        .map(_.metadata.name) should contain (MurasamePoison)
    }
    "apply poison on ability hit" in {
      val newGameState: GameState = gameState.useAbilityOnCharacter(
        eliminateAbilityId,
        s.characters.p1.id
      )
      newGameState
        .characterById(s.characters.p1.id)
        .state
        .effects
        .map(_.metadata.name) should contain (MurasamePoison)
    }

    def attackAndSkip(gameState: GameState): GameState = {
      val n = gameState.basicAttack(
        s.characters.p0.id,
        s.characters.p1.id
      )
      if(n.characterById(s.characters.p1.id).isDead) n
      else n.passTurn(s.characters.p1.id)
    }

    @tailrec
    def attackAndSkipN(n: Int)(gameState: GameState): GameState =
      if(n <= 0) gameState
      else attackAndSkipN(n-1)(attackAndSkip(gameState))

    "stack poison" in {
      val newGameState = attackAndSkipN(2)(gameState)

      newGameState
        .characterById(s.characters.p1.id)
        .state
        .effects
        .map(_.metadata.name).count(_ == MurasamePoison) should be (2)
    }

    "execute target when fully stacked" in {
      val stacksToKill = abilityMetadata.variables("poisonStacksToDie")
      val newGameState = attackAndSkipN(stacksToKill)(gameState)

      newGameState.characterById(s.characters.p1.id).isDead should be (true)

      newGameState
        .gameLog
        .events
        .ofType[CharacterDied] should not be empty
    }
  }
}