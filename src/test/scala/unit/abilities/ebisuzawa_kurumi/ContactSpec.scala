package unit.abilities.ebisuzawa_kurumi

import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.abilities.ebisuzawa_kurumi.Contact
import com.tosware.nkm.models.game.abilities.hecate._
import com.tosware.nkm.models.game.character.CharacterMetadata
import com.tosware.nkm.models.game.event.GameEvent
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
    s.p(0)(0).character.state.abilities(0).id
  private val asterAbilityId =
    s.p(0)(0).character.state.abilities(1).id

  abilityMetadata.name must {
    "be able to deal bonus damage from basic attacks" in {
      val newGameState: GameState = gameState.basicAttack(s.p(0)(0).character.id, s.p(1)(0).character.id)
      newGameState.gameLog.events
        .ofType[GameEvent.AbilityHitCharacter]
        .ofAbility(abilityId)
        .size should be (1)
    }

    "be able to deal bonus damage from an ability" in {
      val newGameState: GameState = gameState.useAbilityOnCoordinates(asterAbilityId, s.p(0)(1).spawnCoordinates)
      newGameState.gameLog.events
        .ofType[GameEvent.AbilityHitCharacter]
        .ofAbility(abilityId)
        .size should be (2)
    }

    "not be able to deal bonus damage more than one time per character" in {
      val newGameState: GameState = gameState
        .basicAttack(s.p(0)(0).character.id, s.p(1)(0).character.id)
        .passTurn(s.p(1)(0).character.id)
        .passTurn(s.p(0)(1).character.id)
        .passTurn(s.p(1)(1).character.id)
        .basicAttack(s.p(0)(0).character.id, s.p(1)(0).character.id)
        .passTurn(s.p(1)(0).character.id)
        .passTurn(s.p(0)(1).character.id)
        .passTurn(s.p(1)(1).character.id)
        .basicAttack(s.p(0)(0).character.id, s.p(1)(0).character.id)

      newGameState.gameLog.events
        .ofType[GameEvent.AbilityHitCharacter]
        .ofAbility(abilityId)
        .size should be (1)
    }
  }
}