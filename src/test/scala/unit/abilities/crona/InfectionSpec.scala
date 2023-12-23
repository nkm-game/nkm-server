package unit.abilities.crona

import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.abilities.crona.Infection
import com.tosware.nkm.models.game.ability.UseData
import com.tosware.nkm.models.game.character.CharacterMetadata
import com.tosware.nkm.models.game.event.GameEvent
import com.tosware.nkm.models.game.hex.TestHexMapName
import helpers.{TestScenario, TestUtils}

class InfectionSpec extends TestUtils {
  private val abilityMetadata = Infection.metadata
  private val characterMetadata = CharacterMetadata.empty().copy(initialAbilitiesMetadataIds = Seq(abilityMetadata.id))
  private val s = TestScenario.generate(TestHexMapName.Simple2v2, characterMetadata)
  implicit private val gameState: GameState = s.ultGs
  private val abilityId = s.defaultAbilityId
  private val abilityId2 = s.p(0)(1).character.state.abilities.head.id

  abilityMetadata.name must {
    "be able to use" in {
      val r = GameStateValidator()
        .validateAbilityUse(s.owners(0), abilityId, UseData(s.defaultEnemy.id))
      assertCommandSuccess(r)
    }

    "be able to infect and deal damage" in {
      val abilityGameState: GameState = gameState.useAbility(abilityId, UseData(s.defaultEnemy.id))
      abilityGameState.gameLog.events
        .ofType[GameEvent.EffectAddedToCharacter]
        .causedBy(abilityId).size shouldBe 1

      val attackGameState =
        abilityGameState
          .endTurn()
          .passTurn(s.p(0)(1).character.id)
          .basicAttack(s.p(0)(1).character.id, s.defaultEnemy.id)

      attackGameState.gameLog.events
        .ofType[GameEvent.CharacterDamaged]
        .causedBy(abilityId)
        .size shouldBe 2
    }

    "be able to trigger loop correctly" in {
      val newGameState: GameState = gameState
        .useAbility(abilityId, UseData(s.defaultEnemy.id))
        .endTurn()
        .passTurn(s.p(0)(1).character.id)
        .useAbility(abilityId2, UseData(s.p(1)(1).character.id))
        .endTurn()
        .passTurn(s.defaultCharacter.id)
        .basicAttack(s.p(0)(1).character.id, s.defaultEnemy.id)

      newGameState.gameLog.events
        .ofType[GameEvent.CharacterDied]
        .size should (be > 0 and be < 3)
    }
  }
}
