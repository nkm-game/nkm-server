package unit.abilities.akame

import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.abilities.akame.{Eliminate, Murasame}
import com.tosware.nkm.models.game.ability.UseData
import com.tosware.nkm.models.game.character.CharacterMetadata
import com.tosware.nkm.models.game.character_effect.CharacterEffectName.*
import com.tosware.nkm.models.game.event.GameEvent.CharacterDied
import com.tosware.nkm.models.game.hex.TestHexMapName
import helpers.{TestScenario, TestUtils}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import scala.annotation.tailrec

class MurasameSpec
    extends AnyWordSpecLike
    with Matchers
    with TestUtils {
  private val abilityMetadata = Murasame.metadata
  private val characterMetadata = CharacterMetadata.empty()
    .copy(initialAbilitiesMetadataIds =
      Seq(
        abilityMetadata.id,
        Eliminate.metadata.id,
      )
    )
  private val s = TestScenario.generate(TestHexMapName.Simple1v1, characterMetadata)
  implicit private val gameState: GameState = s.gameState
  private val eliminateAbilityId = s.defaultCharacter.state.abilities.tail.head.id

  abilityMetadata.name must {
    "apply poison on basic attack" in {
      val newGameState: GameState = gameState.basicAttack(
        s.defaultCharacter.id,
        s.defaultEnemy.id,
      )
      newGameState
        .characterById(s.defaultEnemy.id)
        .state
        .effects
        .map(_.metadata.name) should contain(MurasamePoison)
    }
    "apply poison on ability hit" in {
      val newGameState: GameState = gameState.useAbility(
        eliminateAbilityId,
        UseData(s.defaultEnemy.id),
      )
      newGameState
        .characterById(s.defaultEnemy.id)
        .state
        .effects
        .map(_.metadata.name) should contain(MurasamePoison)
    }

    def attackAndSkip(gameState: GameState): GameState = {
      val n = gameState.basicAttack(
        s.defaultCharacter.id,
        s.defaultEnemy.id,
      )
      if (n.characterById(s.defaultEnemy.id).isDead) n
      else n.passTurn(s.defaultEnemy.id)
    }

    @tailrec
    def attackAndSkipN(n: Int)(gameState: GameState): GameState =
      if (n <= 0) gameState
      else attackAndSkipN(n - 1)(attackAndSkip(gameState))

    "stack poison" in {
      val newGameState = attackAndSkipN(2)(gameState)

      newGameState
        .characterById(s.defaultEnemy.id)
        .state
        .effects
        .map(_.metadata.name).count(_ == MurasamePoison) should be(2)
    }

    "execute target when fully stacked" in {
      val stacksToKill = abilityMetadata.variables("poisonStacksToDie")
      val newGameState = attackAndSkipN(stacksToKill)(gameState)

      newGameState.characterById(s.defaultEnemy.id).isDead should be(true)

      newGameState
        .gameLog
        .events
        .ofType[CharacterDied] should not be empty
    }
  }
}
