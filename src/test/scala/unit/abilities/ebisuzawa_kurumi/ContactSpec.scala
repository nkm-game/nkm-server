package unit.abilities.ebisuzawa_kurumi

import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.abilities.ebisuzawa_kurumi.Contact
import com.tosware.nkm.models.game.abilities.hecate.*
import com.tosware.nkm.models.game.ability.UseData
import com.tosware.nkm.models.game.character.CharacterMetadata
import com.tosware.nkm.models.game.event.GameEvent
import com.tosware.nkm.models.game.hex.TestHexMapName
import helpers.{TestScenario, TestUtils}

class ContactSpec extends TestUtils {
  private val abilityMetadata = Contact.metadata
  private val characterMetadata = CharacterMetadata.empty()
    .copy(
      initialHealthPoints = 10000,
      initialAbilitiesMetadataIds = Seq(
        abilityMetadata.id,
        Aster.metadata.id,
      ),
    )
  private val s = TestScenario.generate(TestHexMapName.Simple2v2, characterMetadata)
  implicit private val gameState: GameState = s.gameState
  private val abilityId =
    s.defaultCharacter.state.abilities(0).id
  private val asterAbilityId =
    s.defaultCharacter.state.abilities(1).id

  abilityMetadata.name must {
    "be able to deal bonus damage from basic attacks" in {
      val newGameState: GameState = gameState.basicAttack(s.defaultCharacter.id, s.defaultEnemy.id)
      newGameState.gameLog.events
        .ofType[GameEvent.AbilityHitCharacter]
        .ofAbility(abilityId)
        .size should be(1)
    }

    "be able to deal bonus damage from an ability" in {
      val newGameState: GameState = gameState.useAbility(asterAbilityId, UseData(s.p(0)(1).spawnCoordinates))
      newGameState.gameLog.events
        .ofType[GameEvent.AbilityHitCharacter]
        .ofAbility(abilityId)
        .size should be(2)
    }

    "not be able to deal bonus damage more than one time per character" in {
      val newGameState: GameState = gameState
        .basicAttack(s.defaultCharacter.id, s.defaultEnemy.id)
        .passTurn(s.defaultEnemy.id)
        .passTurn(s.p(0)(1).character.id)
        .passTurn(s.p(1)(1).character.id)
        .basicAttack(s.defaultCharacter.id, s.defaultEnemy.id)
        .passTurn(s.defaultEnemy.id)
        .passTurn(s.p(0)(1).character.id)
        .passTurn(s.p(1)(1).character.id)
        .basicAttack(s.defaultCharacter.id, s.defaultEnemy.id)

      newGameState.gameLog.events
        .ofType[GameEvent.AbilityHitCharacter]
        .ofAbility(abilityId)
        .size should be(1)
    }
  }
}
