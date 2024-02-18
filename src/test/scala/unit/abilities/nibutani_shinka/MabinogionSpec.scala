package unit.abilities.nibutani_shinka

import com.tosware.nkm.*
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.abilities.nibutani_shinka.Mabinogion
import com.tosware.nkm.models.game.ability.AbilityType
import com.tosware.nkm.models.game.character.CharacterMetadata
import com.tosware.nkm.models.game.event.GameEvent
import com.tosware.nkm.models.game.game_state.GameState
import com.tosware.nkm.models.game.hex.TestHexMapName
import helpers.{TestScenario, TestUtils}

class MabinogionSpec extends TestUtils {
  private val abilityMetadata = Mabinogion.metadata
  private val characterMetadata = CharacterMetadata.empty().copy(initialAbilitiesMetadataIds = Seq(abilityMetadata.id))
  private val s = TestScenario.generate(TestHexMapName.Simple1v9Line, characterMetadata)
  private val gameState: GameState = s.gameState
    .damageCharacters(s.characters.map(_.id), Damage(DamageType.True, 30))
  private val abilityId = s.defaultAbilityId
  private val abilityRange = abilityMetadata.variables("radius")

  private val turnPassedGs = gameState.passTurn(s.defaultCharacter.id)
  private val enchantedTurnPassedGs = gameState
    .addEffect(s.defaultCharacter.id, effects.AbilityEnchant(randomUUID(), 10, AbilityType.Passive))
    .passTurn(s.defaultCharacter.id)

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

    "not immediately remove speed from herself when enchanted" in {
      enchantedTurnPassedGs.gameLog.events
        .ofType[GameEvent.EffectRemovedFromCharacter]
        .size shouldBe 0
    }

    "not add shield above Threshold" in {
      val enchantedShield = enchantedTurnPassedGs.characterById(s.defaultCharacter.id).state.shield

      val shieldSetGs = gameState.setShield(s.defaultCharacter.id, 2)

      shieldSetGs
        .characterById(s.defaultCharacter.id).state.shield shouldBe 2

      shieldSetGs
        .addEffect(s.defaultCharacter.id, effects.AbilityEnchant(randomUUID(), 1, AbilityType.Passive))
        .passTurn(s.defaultCharacter.id)
        .characterById(s.defaultCharacter.id).state.shield shouldBe enchantedShield
    }
  }
}
