package unit.abilities.roronoa_zoro

import com.tosware.nkm.models.{Damage, DamageType, GameStateValidator}
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.abilities.roronoa_zoro.OneHundredEightPoundPhoenix
import com.tosware.nkm.models.game.hex.HexUtils._
import helpers.{Simple2v2TestScenario, TestUtils}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class OneHundredEightPoundPhoenixSpec
  extends AnyWordSpecLike
    with Matchers
    with TestUtils
{
  private val metadata = CharacterMetadata.empty().copy(initialAbilitiesMetadataIds = Seq(OneHundredEightPoundPhoenix.metadata.id))
  private val s = Simple2v2TestScenario(metadata)
  private implicit val gameState: GameState = s.gameState
  private val abilityId = s.characters.p0First.state.abilities.head.id

  OneHundredEightPoundPhoenix.metadata.name must {
    "be able to damage single character" in {
      val r = GameStateValidator()
        .validateAbilityUseOnCharacter(s.characters.p0First.owner.id, abilityId, s.characters.p1First.id)
      assertCommandSuccess(r)

      val newGameState: GameState = gameState.useAbilityOnCharacter(abilityId, s.characters.p1First.id)
      newGameState.gameLog.events.ofType[GameEvent.CharacterDamaged].count(_.causedById == abilityId) shouldBe 3
      newGameState.gameLog.events.ofType[GameEvent.CharacterDamaged].count(_.characterId == s.characters.p1First.id) shouldBe 3
    }
    "be able to damage several characters" in {
      val damagedGameState = gameState.damageCharacter(s.characters.p1First.id, Damage(DamageType.True, 99))(random, gameState.id)
      val r = GameStateValidator()(damagedGameState)
        .validateAbilityUseOnCharacter(s.characters.p0First.owner.id, abilityId, s.characters.p1First.id)
      assertCommandSuccess(r)

      val newGameState: GameState = damagedGameState.useAbilityOnCharacter(abilityId, s.characters.p1First.id)
      newGameState.gameLog.events.ofType[GameEvent.CharacterDamaged].count(_.causedById == abilityId) shouldBe 3
      newGameState.gameLog.events.ofType[GameEvent.CharacterDamaged].ofCharacter(s.characters.p1First.id).count(_.causedById == abilityId) shouldBe 1
      newGameState.gameLog.events.ofType[GameEvent.CharacterDamaged].ofCharacter(s.characters.p1Second.id).count(_.causedById == abilityId) shouldBe 2
    }
    "send shockwaves over friends" in {
      val r = GameStateValidator()
        .validateAbilityUseOnCharacter(s.characters.p0Second.owner.id, abilityId, s.characters.p1First.id)
      assertCommandSuccess(r)
    }
  }
}