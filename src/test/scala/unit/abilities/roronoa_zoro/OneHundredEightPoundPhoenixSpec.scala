package unit.abilities.roronoa_zoro

import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.abilities.roronoa_zoro.OneHundredEightPoundPhoenix
import com.tosware.nkm.models.game.character.CharacterMetadata
import com.tosware.nkm.models.game.event.GameEvent
import helpers.{TestUtils, scenarios}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class OneHundredEightPoundPhoenixSpec
  extends AnyWordSpecLike
    with Matchers
    with TestUtils
{
  private val metadata = CharacterMetadata.empty().copy(initialAbilitiesMetadataIds = Seq(OneHundredEightPoundPhoenix.metadata.id))
  private val s = scenarios.Simple2v2TestScenario(metadata)
  private implicit val gameState: GameState = s.gameState.incrementPhase(4)
  private val abilityId = s.p(0)(0).character.state.abilities.head.id

  OneHundredEightPoundPhoenix.metadata.name must {
    "be able to use" in {
      val r = GameStateValidator()
        .validateAbilityUseOnCharacter(s.p(0)(0).character.owner.id, abilityId, s.p(1)(0).character.id)
      assertCommandSuccess(r)
    }

    "be able to damage single character" in {
      val newGameState: GameState = gameState.useAbilityOnCharacter(abilityId, s.p(1)(0).character.id)
      newGameState.gameLog.events.ofType[GameEvent.CharacterDamaged].count(_.causedById == abilityId) shouldBe 3
      newGameState.gameLog.events.ofType[GameEvent.CharacterDamaged].count(_.characterId == s.p(1)(0).character.id) shouldBe 3
    }

    "be able to damage several characters" in {
      val damagedGameState = gameState.damageCharacter(s.p(1)(0).character.id, Damage(DamageType.True, 99))(random, gameState.id)
      val r = GameStateValidator()(damagedGameState)
        .validateAbilityUseOnCharacter(s.p(0)(0).character.owner.id, abilityId, s.p(1)(0).character.id)
      assertCommandSuccess(r)

      val newGameState: GameState = damagedGameState.useAbilityOnCharacter(abilityId, s.p(1)(0).character.id)
      newGameState.gameLog.events.ofType[GameEvent.CharacterDamaged].count(_.causedById == abilityId) shouldBe 3
      newGameState.gameLog.events.ofType[GameEvent.CharacterDamaged].ofCharacter(s.p(1)(0).character.id).count(_.causedById == abilityId) shouldBe 1
      newGameState.gameLog.events.ofType[GameEvent.CharacterDamaged].ofCharacter(s.p(1)(1).character.id).count(_.causedById == abilityId) shouldBe 2
    }

    "send shockwaves over friends" in {
      val r = GameStateValidator()
        .validateAbilityUseOnCharacter(s.p(0)(1).character.owner.id, abilityId, s.p(1)(0).character.id)
      assertCommandSuccess(r)
    }
  }
}