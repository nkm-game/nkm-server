package unit.abilities.crona

import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.abilities.crona.ScreechAlpha
import com.tosware.nkm.models.game.character.CharacterMetadata
import com.tosware.nkm.models.game.character_effect.CharacterEffectName._
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
  private val abilityId = s.characters.p0.state.abilities.head.id
  private val abilityRadius = abilityMetadata.variables("radius")

  abilityMetadata.name must {
    "be able to use" in {
      val r = GameStateValidator()
        .validateAbilityUse(s.characters.p0.owner.id, abilityId)
      assertCommandSuccess(r)
    }

    "be able to stun and slow nearby enemies" in {
      val newGameState: GameState = gameState.useAbility(abilityId)
      newGameState.gameLog.events
        .ofType[GameEvent.EffectAddedToCharacter]
        .causedBy(abilityId).size shouldBe abilityRadius * 2

      val coordsInRange = s.characters.p0.parentCell.get.coordinates.getCircle(abilityRadius)

      s.characters.p1.foreach { p =>
        val effectsNames = newGameState.characterById(p.id)
          .state
          .effects
          .map(_.metadata.name)
        if(coordsInRange.contains(p.parentCell.get.coordinates)) {
          effectsNames should (contain (Stun) and contain (StatNerf))
        } else {
          effectsNames should be (empty)
        }
      }
    }
  }
}