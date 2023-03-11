package unit.abilities.shana

import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.abilities.shana.WingsOfCrimson
import com.tosware.nkm.models.game.character.{CharacterMetadata, StatType}
import helpers.{TestUtils, scenarios}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class WingsOfCrimsonSpec
  extends AnyWordSpecLike
    with Matchers
    with TestUtils
{
  private val abilityMetadata = WingsOfCrimson.metadata
  private val characterMetadata = CharacterMetadata.empty().copy(initialAbilitiesMetadataIds = Seq(abilityMetadata.id))
  private val s = scenarios.Simple1v1TestScenario(characterMetadata)
  private val gameState: GameState = s.gameState.passTurn(s.characters.p0.id)

  abilityMetadata.name must {
    "apply flying and speed buff effects on being basic attacked" in {
      val ngs: GameState = gameState.basicAttack(s.p(1)(0).character.id, s.p(0)(0).character.id)

      assertEffectExistsOfType[effects.Fly](s.p(0)(0).character.id)(ngs)
      assertBuffExists(StatType.Speed, s.p(0)(0).character.id)(ngs)
    }

    "apply flying and speed buff effects on being damaged" in {
      val ngs: GameState = gameState.damageCharacter(s.p(0)(0).character.id, Damage(DamageType.True, 1))

      assertEffectExistsOfType[effects.Fly](s.p(0)(0).character.id)(ngs)
      assertBuffExists(StatType.Speed, s.p(0)(0).character.id)(ngs)
    }
  }
}