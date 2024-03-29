package unit.abilities.liones_elizabeth

import com.tosware.nkm.*
import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.abilities.liones_elizabeth.PowerOfTheGoddess
import com.tosware.nkm.models.game.character.CharacterMetadata
import com.tosware.nkm.models.game.event.GameEvent
import com.tosware.nkm.models.game.game_state.GameState
import com.tosware.nkm.models.game.hex.TestHexMapName
import helpers.{TestScenario, TestUtils}

class PowerOfTheGodessSpec extends TestUtils {
  private val abilityMetadata = PowerOfTheGoddess.metadata
  private val metadata = CharacterMetadata.empty().copy(initialAbilitiesMetadataIds = Seq(abilityMetadata.id))
  private val s = TestScenario.generate(TestHexMapName.Simple2v2, metadata)
  private val gameState: GameState = s.ultGs
    .damageCharacter(s.defaultCharacter.id, Damage(DamageType.True, 1))
    .damageCharacter(s.p(0)(1).character.id, Damage(DamageType.True, 1))
  private val abilityId = s.defaultAbilityId

  abilityMetadata.name must {
    "be able to use" in {
      assertCommandSuccess {
        GameStateValidator()(gameState)
          .validateAbilityUse(s.owners(0), abilityId)
      }
    }

    "be able to heal characters" in {
      val ags: GameState = gameState.useAbility(abilityId)

      ags.gameLog.events
        .ofType[GameEvent.CharacterHealed]
        .causedBy(abilityId)
        .size should be(2)
    }
  }
}
