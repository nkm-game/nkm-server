package unit.abilities.crona

import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.abilities.crona.BlackBlood
import helpers.{TestUtils, scenarios}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class BlackBloodSpec
  extends AnyWordSpecLike
    with Matchers
    with TestUtils
{
  private val abilityMetadata = BlackBlood.metadata
  private val characterMetadata = CharacterMetadata.empty().copy(initialAbilitiesMetadataIds = Seq(abilityMetadata.id))
  private val s = scenarios.Simple1v9LineTestScenario(characterMetadata)
  private implicit val gameState: GameState = s.gameState.passTurn(s.characters.p0.id)
  private val abilityId = s.characters.p0.state.abilities.head.id
  private val abilityRange = abilityMetadata.variables("radius")
  private val attackingCharacter = s.characters.p1.head

  abilityMetadata.name must {
    "be able to use" in {
      val r = GameStateValidator()
        .validateBasicAttackCharacter(
          attackingCharacter.owner.id,
          attackingCharacter.id,
          s.characters.p0.id,
        )
      assertCommandSuccess(r)
    }

    "deal damage to surrounding enemies" in {
      val newGameState: GameState = gameState.basicAttack(attackingCharacter.id, s.characters.p0.id)
      newGameState.gameLog.events
        .ofType[GameEvent.CharacterDamaged]
        .causedBy(abilityId).size shouldBe abilityRange

      val coordsInRange = s.characters.p0.parentCell.get.coordinates.getCircle(abilityRange)

      s.characters.p1.foreach { p =>
        val state = newGameState.characterById(p.id)
          .state
        val hp = state.healthPoints
        val maxHp = state.maxHealthPoints
        if(coordsInRange.contains(p.parentCell.get.coordinates)) {
          hp should be < maxHp
        } else {
          hp should be (maxHp)
        }
      }
    }
  }
}