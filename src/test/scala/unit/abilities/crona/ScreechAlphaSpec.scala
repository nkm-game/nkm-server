package unit.abilities.crona

import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.abilities.crona.ScreechAlpha
import com.tosware.nkm.models.game.character.CharacterMetadata
import com.tosware.nkm.models.game.character_effect.CharacterEffectName.*
import com.tosware.nkm.models.game.event.GameEvent
import helpers.{TestUtils, scenarios}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class ScreechAlphaSpec
  extends AnyWordSpecLike
    with Matchers
    with TestUtils
{
  private val abilityMetadata = ScreechAlpha.metadata
  private val characterMetadata = CharacterMetadata.empty().copy(initialAbilitiesMetadataIds = Seq(abilityMetadata.id))
  private val s = scenarios.Simple1v9LineTestScenario(characterMetadata)
  private implicit val gameState: GameState = s.gameState
  private val abilityId = s.p(0)(0).character.state.abilities.head.id
  private val abilityRadius = abilityMetadata.variables("radius")

  abilityMetadata.name must {
    "be able to use" in {
      val r = GameStateValidator()
        .validateAbilityUse(s.p(0)(0).character.owner.id, abilityId)
      assertCommandSuccess(r)
    }

    "be able to stun and slow nearby enemies" in {
      val newGameState: GameState = gameState.useAbility(abilityId)
      newGameState.gameLog.events
        .ofType[GameEvent.EffectAddedToCharacter]
        .causedBy(abilityId).size shouldBe abilityRadius * 2

      val coordsInRange = s.p(0)(0).character.parentCell.get.coordinates.getCircle(abilityRadius)

      s.p(1).map(_.character).foreach { c =>
        if(coordsInRange.contains(c.parentCell.get.coordinates)) {
          assertEffectsExist(Seq(Stun, StatNerf), c.id)(newGameState)
        } else {
          assertEffectsDoNotExist(Seq(Stun, StatNerf), c.id)(newGameState)
        }
      }
    }
  }
}