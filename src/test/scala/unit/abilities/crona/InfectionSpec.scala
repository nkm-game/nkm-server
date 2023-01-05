package unit.abilities.crona

import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.abilities.crona.Infection
import com.tosware.nkm.NkmUtils._
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
  private val abilityId = s.characters.p0First.state.abilities.head.id
  private val abilityId2 = s.characters.p0Second.state.abilities.head.id

  abilityMetadata.name must {
    "be able to use" in {
      val r = GameStateValidator()
        .validateAbilityUseOnCharacter(s.characters.p0First.owner.id, abilityId, s.characters.p1First.id)
      assertCommandSuccess(r)
    }

    "be able to infect and deal damage" in {
      val abilityGameState: GameState = gameState.useAbilityOnCharacter(abilityId, s.characters.p1First.id)
      abilityGameState.gameLog.events
        .ofType[GameEvent.EffectAddedToCharacter]
        .causedBy(abilityId).size shouldBe 1

      val attackGameState =
        abilityGameState
          .endTurn()
          .passTurn(s.characters.p0Second.id)
          .basicAttack(s.characters.p0Second.id, s.characters.p1First.id)

      attackGameState.gameLog.events
        .ofType[GameEvent.CharacterDamaged]
        .causedBy(abilityId)
        .size shouldBe 2
    }

    "be able to trigger loop correctly" in {
      val newGameState: GameState = gameState
        .useAbilityOnCharacter(abilityId, s.characters.p1First.id)
        .endTurn()
        .passTurn(s.characters.p0Second.id)
        .useAbilityOnCharacter(abilityId2, s.characters.p1Second.id)
        .endTurn()
        .passTurn(s.characters.p0First.id)
        .basicAttack(s.characters.p0Second.id, s.characters.p1First.id)

      newGameState.gameLog.events
        .ofType[GameEvent.CharacterDied]
        .size should (be > 0 and be < 3)
    }
  }
}