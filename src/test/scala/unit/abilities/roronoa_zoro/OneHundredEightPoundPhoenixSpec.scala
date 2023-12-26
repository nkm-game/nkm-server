package unit.abilities.roronoa_zoro

import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.abilities.roronoa_zoro.OneHundredEightPoundPhoenix
import com.tosware.nkm.models.game.ability.UseData
import com.tosware.nkm.models.game.character.CharacterMetadata
import com.tosware.nkm.models.game.event.GameEvent
import com.tosware.nkm.models.game.game_state.GameState
import com.tosware.nkm.models.game.hex.TestHexMapName
import helpers.{TestScenario, TestUtils}

class OneHundredEightPoundPhoenixSpec extends TestUtils {
  private val metadata =
    CharacterMetadata.empty().copy(initialAbilitiesMetadataIds = Seq(OneHundredEightPoundPhoenix.metadata.id))
  private val s = TestScenario.generate(TestHexMapName.Simple2v2, metadata)
  implicit private val gameState: GameState = s.ultGs
  private val abilityId = s.defaultAbilityId

  OneHundredEightPoundPhoenix.metadata.name must {
    "be able to use" in {
      val r = GameStateValidator()
        .validateAbilityUse(s.owners(0), abilityId, UseData(s.defaultEnemy.id))
      assertCommandSuccess(r)
    }

    "be able to damage single character" in {
      val newGameState: GameState = gameState.useAbility(abilityId, UseData(s.defaultEnemy.id))
      newGameState.gameLog.events.ofType[GameEvent.CharacterDamaged].count(_.causedById == abilityId) shouldBe 3
      newGameState.gameLog.events.ofType[GameEvent.CharacterDamaged].count(
        _.characterId == s.defaultEnemy.id
      ) shouldBe 3
    }

    "be able to damage several characters" in {
      val damagedGameState =
        gameState.damageCharacter(s.defaultEnemy.id, Damage(DamageType.True, 99))(random, gameState.id)
      val r = GameStateValidator()(damagedGameState)
        .validateAbilityUse(s.owners(0), abilityId, UseData(s.defaultEnemy.id))
      assertCommandSuccess(r)

      val newGameState: GameState = damagedGameState.useAbility(abilityId, UseData(s.defaultEnemy.id))
      newGameState.gameLog.events.ofType[GameEvent.CharacterDamaged].count(_.causedById == abilityId) shouldBe 3
      newGameState.gameLog.events.ofType[GameEvent.CharacterDamaged].ofCharacter(s.defaultEnemy.id).count(
        _.causedById == abilityId
      ) shouldBe 1
      newGameState.gameLog.events.ofType[GameEvent.CharacterDamaged].ofCharacter(s.p(1)(1).character.id).count(
        _.causedById == abilityId
      ) shouldBe 2
    }

    "send shockwaves over friends" in {
      val r = GameStateValidator()
        .validateAbilityUse(s.p(0)(1).character.owner.id, abilityId, UseData(s.defaultEnemy.id))
      assertCommandSuccess(r)
    }
  }
}
