package unit.abilities.crona

import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.abilities.crona.Infection
import com.tosware.nkm.models.game.character.CharacterMetadata
import com.tosware.nkm.models.game.event.GameEvent
import helpers.{TestUtils, scenarios}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class InfectionSpec
  extends AnyWordSpecLike
    with Matchers
    with TestUtils
{
  private val abilityMetadata = Infection.metadata
  private val characterMetadata = CharacterMetadata.empty().copy(initialAbilitiesMetadataIds = Seq(abilityMetadata.id))
  private val s = scenarios.Simple2v2TestScenario(characterMetadata)
  private implicit val gameState: GameState = s.gameState.incrementPhase(4)
  private val abilityId = s.p(0)(0).character.state.abilities.head.id
  private val abilityId2 = s.p(0)(1).character.state.abilities.head.id

  abilityMetadata.name must {
    "be able to use" in {
      val r = GameStateValidator()
        .validateAbilityUseOnCharacter(s.p(0)(0).character.owner.id, abilityId, s.p(1)(0).character.id)
      assertCommandSuccess(r)
    }

    "be able to infect and deal damage" in {
      val abilityGameState: GameState = gameState.useAbilityOnCharacter(abilityId, s.p(1)(0).character.id)
      abilityGameState.gameLog.events
        .ofType[GameEvent.EffectAddedToCharacter]
        .causedBy(abilityId).size shouldBe 1

      val attackGameState =
        abilityGameState
          .endTurn()
          .passTurn(s.p(0)(1).character.id)
          .basicAttack(s.p(0)(1).character.id, s.p(1)(0).character.id)

      attackGameState.gameLog.events
        .ofType[GameEvent.CharacterDamaged]
        .causedBy(abilityId)
        .size shouldBe 2
    }

    "be able to trigger loop correctly" in {
      val newGameState: GameState = gameState
        .useAbilityOnCharacter(abilityId, s.p(1)(0).character.id)
        .endTurn()
        .passTurn(s.p(0)(1).character.id)
        .useAbilityOnCharacter(abilityId2, s.p(1)(1).character.id)
        .endTurn()
        .passTurn(s.p(0)(0).character.id)
        .basicAttack(s.p(0)(1).character.id, s.p(1)(0).character.id)

      newGameState.gameLog.events
        .ofType[GameEvent.CharacterDied]
        .size should (be > 0 and be < 3)
    }
  }
}