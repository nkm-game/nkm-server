package unit.abilities.ayatsuji_ayase

import com.tosware.nkm.models.game.abilities.ayatsuji_ayase.SamuraisSwiftness
import com.tosware.nkm.models.game.character.{CharacterMetadata, StatType}
import com.tosware.nkm.models.game.effects.StatBuff
import helpers.{TestUtils, scenarios}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class SamuraisSwiftnessSpec
  extends AnyWordSpecLike
    with Matchers
    with TestUtils
{
  private val abilityMetadata = SamuraisSwiftness.metadata
  private val metadata = CharacterMetadata.empty().copy(initialAbilitiesMetadataIds = Seq(abilityMetadata.id))
  private val s = scenarios.Simple1v1TestScenario(metadata)

  abilityMetadata.name must {
    "give speed buff turn after damaging someone" in {
      val damagedGameState = s.gameState.basicAttack(s.characters.p0.id, s.characters.p1.id)
      val newTurnGameState = damagedGameState.endTurn().passTurn(s.characters.p1.id)

      {
        val statBuffs = damagedGameState.characterById(s.characters.p0.id).state.effects.ofType[StatBuff]
        statBuffs should be (empty)
      }
      {
        val statBuffs = newTurnGameState.characterById(s.characters.p0.id).state.effects.ofType[StatBuff]
        statBuffs should not be empty
        statBuffs.head.statType should be (StatType.Speed)
      }
    }
  }
}