package unit.abilities.kirito

import com.tosware.nkm.models.game.abilities.kirito.Parry
import com.tosware.nkm.models.game.character.CharacterMetadata
import helpers.{TestUtils, scenarios}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class ParrySpec
  extends AnyWordSpecLike
    with Matchers
    with TestUtils
{
  private val initialHp = 10000
  private val abilityMetadata = Parry.metadata
  private val characterMetadata = CharacterMetadata.empty()
    .copy(
      initialHealthPoints = initialHp,
      initialAbilitiesMetadataIds = Seq(abilityMetadata.id))
  private val s = scenarios.Simple1v1TestScenario(characterMetadata)

  abilityMetadata.name must {
    "make parent character block attacks sometimes" in {
      def attack() = s.gameState.basicAttack(s.characters.p0.id, s.characters.p1.id)
      def moveAndGetHpChanged() = {
        val attackGameState = attack()
        attackGameState.characterById(s.characters.p1.id).state.healthPoints != initialHp
      }
      val results = (0 to 50).map(_ => moveAndGetHpChanged())
      results.toSet should be (Set(true, false))
    }
  }
}