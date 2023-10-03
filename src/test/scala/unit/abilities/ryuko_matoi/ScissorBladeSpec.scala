package unit.abilities.ryuko_matoi

import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.abilities.ryuko_matoi.ScissorBlade
import com.tosware.nkm.models.game.character.{CharacterMetadata, StatType}
import com.tosware.nkm.models.game.effects.StatNerf
import helpers.{TestUtils, scenarios}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class ScissorBladeSpec
    extends AnyWordSpecLike
    with Matchers
    with TestUtils {

  private val abilityMetadata = ScissorBlade.metadata
  private val characterMetadata = CharacterMetadata.empty().copy(initialAbilitiesMetadataIds = Seq(abilityMetadata.id))
  private val s = scenarios.Simple1v1TestScenario(characterMetadata)
  implicit private val gameState: GameState = s.gameState

  abilityMetadata.name must {
    "decrease target physical defense" in {
      val attackedGameState = gameState.basicAttack(s.p(0)(0).character.id, s.p(1)(0).character.id)
      val statNerfEffects = attackedGameState.characterById(s.p(1)(0).character.id).state.effects.ofType[StatNerf]
      statNerfEffects should not be empty
      statNerfEffects.head.statType should be(StatType.PhysicalDefense)
    }
  }
}
