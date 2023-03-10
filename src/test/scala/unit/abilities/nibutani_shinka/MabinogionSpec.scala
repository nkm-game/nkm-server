package unit.abilities.nibutani_shinka

import com.tosware.nkm._
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.abilities.nibutani_shinka.Mabinogion
import com.tosware.nkm.models.game.ability.AbilityType
import com.tosware.nkm.models.game.character.CharacterMetadata
import com.tosware.nkm.models.game.event.GameEvent
import helpers.{TestUtils, scenarios}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class MabinogionSpec
  extends AnyWordSpecLike
    with Matchers
    with TestUtils
{
  private val abilityMetadata = Mabinogion.metadata
  private val characterMetadata = CharacterMetadata.empty().copy(initialAbilitiesMetadataIds = Seq(abilityMetadata.id))
  private val s = scenarios.Simple1v9LineTestScenario(characterMetadata)
  private val gameState: GameState = s.gameState
  private val abilityId = s.characters.p0.state.abilities.head.id
  private val abilityRange = abilityMetadata.variables("radius")

  private val turnPassedGs = gameState.passTurn(s.characters.p0.id)
  private val enchantedTurnPassedGs = gameState
    .addEffect(s.characters.p0.id, effects.AbilityEnchant(randomUUID(), 1, AbilityType.Passive))
    .passTurn(s.characters.p0.id)

  private val healEvents = turnPassedGs.gameLog.events
    .ofType[GameEvent.CharacterHealed]
    .causedBy(abilityId)

  private val shieldEvents = turnPassedGs.gameLog.events
    .ofType[GameEvent.CharacterShieldSet]
    .causedBy(abilityId)

  private val enchantedHealEvents = enchantedTurnPassedGs.gameLog.events
    .ofType[GameEvent.CharacterHealed]
    .causedBy(abilityId)

  private val enchantedShieldEvents = enchantedTurnPassedGs.gameLog.events
    .ofType[GameEvent.CharacterShieldSet]
    .causedBy(abilityId)

  abilityMetadata.name must {
    "heal all nearby characters" in {
      healEvents.size shouldBe abilityRange + 1
    }
    "shield only friendly characters" in {
      shieldEvents.size shouldBe 1
    }

    "heal only friendly characters when enchanted" in {
      enchantedHealEvents.size shouldBe 1
    }

    "triple heal when enchanted" in {
      healEvents.head.amount * 3 shouldBe enchantedHealEvents.head.amount
    }

    "triple shield when enchanted" in {
      shieldEvents.head.amount * 3 shouldBe enchantedShieldEvents.head.amount
    }

    "add speed to friends only when enchanted" in {
      turnPassedGs.gameLog.events.ofType[GameEvent.EffectAddedToCharacter].causedBy(abilityId).size shouldBe 0
      enchantedTurnPassedGs.gameLog.events.ofType[GameEvent.EffectAddedToCharacter].causedBy(abilityId).size shouldBe 1
    }

    "not add shield above treshold" in {
      val enchantedShield = enchantedTurnPassedGs.characterById(s.characters.p0.id).state.shield

      val shieldSetGs = gameState.setShield(s.characters.p0.id, 2)

      shieldSetGs
        .passTurn(s.characters.p0.id)
        .characterById(s.characters.p0.id).state.shield shouldBe 2

      shieldSetGs
        .addEffect(s.characters.p0.id, effects.AbilityEnchant(randomUUID(), 1, AbilityType.Passive))
        .passTurn(s.characters.p0.id)
        .characterById(s.characters.p0.id).state.shield shouldBe enchantedShield
    }
  }
}