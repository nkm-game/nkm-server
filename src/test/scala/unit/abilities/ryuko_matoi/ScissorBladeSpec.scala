package unit.abilities.ryuko_matoi

import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.abilities.ryuko_matoi.ScissorBlade
import com.tosware.nkm.models.game.effects.StatNerf
import com.tosware.nkm.models.game.hex.HexUtils.SeqUtils
import helpers.{TestUtils, scenarios}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class ScissorBladeSpec
  extends AnyWordSpecLike
    with Matchers
    with TestUtils
{

  private val abilityMetadata = ScissorBlade.metadata
  private val characterMetadata = CharacterMetadata.empty().copy(initialAbilitiesMetadataIds = Seq(abilityMetadata.id))
  private val s = scenarios.Simple1v1TestScenario(characterMetadata)
  private implicit val gameState: GameState = s.gameState

  abilityMetadata.name must {
    "decrease target physical defense" in {
      val attackedGameState = gameState.basicAttack(s.characters.p0.id, s.characters.p1.id)
      val statNerfEffects = attackedGameState.characterById(s.characters.p1.id).get.state.effects.ofType[StatNerf]
      statNerfEffects should not be empty
      statNerfEffects.head.statType should be (StatType.PhysicalDefense)
    }
  }
}