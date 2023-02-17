package unit.abilities.ebisuzawa_kurumi

import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.abilities.ebisuzawa_kurumi.Contact
import com.tosware.nkm.models.game.abilities.hecate._
import com.tosware.nkm.models.game.character.CharacterMetadata
import helpers.{TestUtils, scenarios}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class ContactSpec
  extends AnyWordSpecLike
    with Matchers
    with TestUtils
{
  private val abilityMetadata = Contact.metadata
  private val characterMetadata = CharacterMetadata.empty()
    .copy(
      initialHealthPoints = 10000,
      initialAbilitiesMetadataIds = Seq(
        abilityMetadata.id,
        Aster.metadata.id,
      ),
    )
  private val s = scenarios.Simple2v2TestScenario(characterMetadata)
  private implicit val gameState: GameState = s.gameState
  private val abilityId =
    s.characters.p0First.state.abilities(0).id
  private val asterAbilityId =
    s.characters.p0First.state.abilities(1).id

  abilityMetadata.name must {
    "be able to deal bonus damage from basic attacks" in {
      val newGameState: GameState = gameState.basicAttack(s.characters.p0First.id, s.characters.p1First.id)
      newGameState.gameLog.events
        .ofType[GameEvent.AbilityHitCharacter]
        .ofAbility(abilityId)
        .size should be (1)
    }

    "be able to deal bonus damage from an ability" in {
      val newGameState: GameState = gameState.useAbilityOnCoordinates(asterAbilityId, s.spawnCoordinates.p0Second)
      newGameState.gameLog.events
        .ofType[GameEvent.AbilityHitCharacter]
        .ofAbility(abilityId)
        .size should be (2)
    }

    "not be able to deal bonus damage more than one time per character" in {
      val newGameState: GameState = gameState
        .basicAttack(s.characters.p0First.id, s.characters.p1First.id)
        .passTurn(s.characters.p1First.id)
        .passTurn(s.characters.p0Second.id)
        .passTurn(s.characters.p1Second.id)
        .basicAttack(s.characters.p0First.id, s.characters.p1First.id)
        .passTurn(s.characters.p1First.id)
        .passTurn(s.characters.p0Second.id)
        .passTurn(s.characters.p1Second.id)
        .basicAttack(s.characters.p0First.id, s.characters.p1First.id)

      newGameState.gameLog.events
        .ofType[GameEvent.AbilityHitCharacter]
        .ofAbility(abilityId)
        .size should be (1)
    }
  }
}