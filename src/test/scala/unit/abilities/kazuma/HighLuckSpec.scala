package unit.abilities.kazuma

import com.tosware.nkm.models.game.abilities.satou_kazuma.HighLuck
import com.tosware.nkm.models.game.character.CharacterMetadata
import com.tosware.nkm.models.game.event.GameEvent.CharacterDamaged
import com.tosware.nkm.models.game.hex.TestHexMapName
import helpers.{TestScenario, TestUtils}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class HighLuckSpec
    extends AnyWordSpecLike
    with Matchers
    with TestUtils {
  private val initialHp = 10000
  private val abilityMetadata = HighLuck.metadata
  private val characterMetadata = CharacterMetadata.empty()
    .copy(
      initialHealthPoints = initialHp,
      initialAbilitiesMetadataIds = Seq(abilityMetadata.id),
    )
  private val s = TestScenario.generate(TestHexMapName.Simple1v1, characterMetadata)

  abilityMetadata.name must {
    "critically strike sometimes with basic attacks" in {
      def generateAttackGs() = s.gameState.basicAttack(s.defaultCharacter.id, s.defaultEnemy.id)
      def damageAmount() =
        generateAttackGs()
          .gameLog.events.ofType[CharacterDamaged].head.damageAmount
      (0 to 50).map(_ => damageAmount()).toSet.size should be(2)
    }
  }
}
