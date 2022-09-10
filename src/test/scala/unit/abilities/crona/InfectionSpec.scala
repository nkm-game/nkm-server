package unit.abilities.crona

import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.abilities.crona.Infection
import com.tosware.nkm.models.game.hex.HexUtils._
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
  private implicit val gameState: GameState = s.gameState
  private val abilityId = s.characters.p0First.state.abilities.head.id

  abilityMetadata.name must {
    "be able to infect and deal damage" in {
      val r = GameStateValidator()
        .validateAbilityUseOnCharacter(s.characters.p0First.owner.id, abilityId, s.characters.p1First.id)
      assertCommandSuccess(r)

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
  }
}