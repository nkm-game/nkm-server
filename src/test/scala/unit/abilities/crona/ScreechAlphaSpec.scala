package unit.abilities.crona

import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.abilities.crona.ScreechAlpha
import com.tosware.nkm.models.game.character.CharacterMetadata
import com.tosware.nkm.models.game.character_effect.CharacterEffectName.*
import com.tosware.nkm.models.game.event.GameEvent
import com.tosware.nkm.models.game.hex.TestHexMapName
import helpers.{TestScenario, TestUtils}

class ScreechAlphaSpec extends TestUtils {
  private val abilityMetadata = ScreechAlpha.metadata
  private val characterMetadata = CharacterMetadata.empty().copy(initialAbilitiesMetadataIds = Seq(abilityMetadata.id))
  private val s = TestScenario.generate(TestHexMapName.Simple1v9Line, characterMetadata)
  implicit private val gameState: GameState = s.gameState
  private val abilityId = s.defaultAbilityId
  private val abilityRadius = abilityMetadata.variables("radius")

  abilityMetadata.name must {
    "be able to use" in {
      val r = GameStateValidator()
        .validateAbilityUse(s.owners(0), abilityId)
      assertCommandSuccess(r)
    }

    "be able to stun and slow nearby enemies" in {
      val newGameState: GameState = gameState.useAbility(abilityId)
      newGameState.gameLog.events
        .ofType[GameEvent.EffectAddedToCharacter]
        .causedBy(abilityId).size shouldBe abilityRadius * 2

      val coordsInRange = s.defaultCharacter.parentCellOpt.get.coordinates.getCircle(abilityRadius)

      s.p(1).map(_.character).foreach { c =>
        if (coordsInRange.contains(c.parentCellOpt.get.coordinates)) {
          assertEffectsExist(Seq(Stun, StatNerf), c.id)(newGameState)
        } else {
          assertEffectsDoNotExist(Seq(Stun, StatNerf), c.id)(newGameState)
        }
      }
    }
  }
}
