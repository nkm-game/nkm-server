package unit.abilities.crona

import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.abilities.crona.BlackBlood
import com.tosware.nkm.models.game.character.CharacterMetadata
import com.tosware.nkm.models.game.event.GameEvent
import helpers.{TestUtils, scenarios}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class BlackBloodSpec
    extends AnyWordSpecLike
    with Matchers
    with TestUtils {
  private val abilityMetadata = BlackBlood.metadata
  private val characterMetadata = CharacterMetadata.empty().copy(initialAbilitiesMetadataIds = Seq(abilityMetadata.id))
  private val s = scenarios.Simple1v9LineTestScenario(characterMetadata)
  private val gameState: GameState = s.gameState.passTurn(s.p(0)(0).character.id)
  private val abilityId = s.p(0)(0).character.state.abilities.head.id
  private val abilityRange = abilityMetadata.variables("radius")
  private val attackingCharacter = s.p(1)(0).character

  abilityMetadata.name must {
    "deal damage to surrounding enemies" in {
      val newGameState: GameState = gameState.basicAttack(attackingCharacter.id, s.p(0)(0).character.id)
      newGameState.gameLog.events
        .ofType[GameEvent.CharacterDamaged]
        .causedBy(abilityId).size shouldBe abilityRange

      val coordsInRange = s.p(0)(0).character.parentCellOpt(newGameState).get.coordinates.getCircle(abilityRange)

      s.p(1).map(_.character).foreach { c =>
        val state = newGameState.characterById(c.id).state
        val hp = state.healthPoints
        val maxHp = state.maxHealthPoints
        if (coordsInRange.contains(c.parentCellOpt(newGameState).get.coordinates)) {
          hp should be < maxHp
        } else {
          hp should be(maxHp)
        }
      }
    }
  }
}
