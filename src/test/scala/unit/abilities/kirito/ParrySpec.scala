package unit.abilities.kirito

import com.tosware.nkm.models.game.abilities.kirito.Parry
import com.tosware.nkm.models.game.character.CharacterMetadata
import com.tosware.nkm.models.game.hex.TestHexMapName
import helpers.{TestScenario, TestUtils}

class ParrySpec extends TestUtils {
  private val initialHp = 10000
  private val abilityMetadata = Parry.metadata
  private val characterMetadata = CharacterMetadata.empty()
    .copy(
      initialHealthPoints = initialHp,
      initialAbilitiesMetadataIds = Seq(abilityMetadata.id),
    )
  private val s = TestScenario.generate(TestHexMapName.Simple1v1, characterMetadata)

  abilityMetadata.name must {
    "make parent character block attacks sometimes" in {
      def attack() = s.gameState.basicAttack(s.defaultCharacter.id, s.defaultEnemy.id)
      def moveAndGetHpChanged() = {
        val attackGameState = attack()
        attackGameState.characterById(s.defaultEnemy.id).state.healthPoints != initialHp
      }
      val results = (0 to 50).map(_ => moveAndGetHpChanged())
      results.toSet should be(Set(true, false))
    }
  }
}
