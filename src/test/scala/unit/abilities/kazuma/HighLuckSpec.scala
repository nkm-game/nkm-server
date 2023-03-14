package unit.abilities.kazuma

import com.tosware.nkm.models.game.abilities.satou_kazuma.HighLuck
import com.tosware.nkm.models.game.character.CharacterMetadata
import com.tosware.nkm.models.game.event.GameEvent.CharacterDamaged
import helpers.{TestUtils, scenarios}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class HighLuckSpec
  extends AnyWordSpecLike
    with Matchers
    with TestUtils
{
  private val initialHp = 10000
  private val abilityMetadata = HighLuck.metadata
  private val characterMetadata = CharacterMetadata.empty()
    .copy(
      initialHealthPoints = initialHp,
      initialAbilitiesMetadataIds = Seq(abilityMetadata.id))
  private val s = scenarios.Simple1v1TestScenario(characterMetadata)

  abilityMetadata.name must {
    "critically strike sometimes with basic attacks" in {
      def generateAttackGs() = s.gameState.basicAttack(s.p(0)(0).character.id, s.p(1)(0).character.id)
      def damageAmount() = {
        generateAttackGs()
          .gameLog.events.ofType[CharacterDamaged].head.damage.amount
      }
      (0 to 50).map(_ => damageAmount()).toSet.size should be (2)
    }
  }
}