package unit.abilities.blank

import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.abilities.blank.*
import com.tosware.nkm.models.game.ability.UseData
import com.tosware.nkm.models.game.character.CharacterMetadata
import helpers.{TestUtils, scenarios}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class AceInTheHoleSpec
    extends AnyWordSpecLike
    with Matchers
    with TestUtils {
  private val abilityMetadata = AceInTheHole.metadata
  private val characterMetadata = CharacterMetadata.empty()
    .copy(initialAbilitiesMetadataIds =
      Seq(
        abilityMetadata.id,
        Check.metadata.id,
        Castling.metadata.id,
      )
    )
  private val s = scenarios.Simple1v1TestScenario(characterMetadata)
  implicit private val gameState: GameState = s.gameState
  private val damagedGameState = gameState.damageCharacter(
    s.p(0)(0).character.id,
    Damage(DamageType.True, (s.p(0)(0).character.state.maxHealthPoints * 0.5).toInt),
  )(random, gameState.id)
  private val checkAbilityId = s.p(0)(0).character.state.abilities(1).id
  private val castlingAbilityId = s.p(0)(0).character.state.abilities(2).id

  abilityMetadata.name must {
    "be able to use any free ability" when {
      "free ability was not on CD" in {
        assertCommandSuccess {
          GameStateValidator()(damagedGameState)
            .validateAbilityUseOnCharacter(s.p(0)(0).character.owner.id, checkAbilityId, s.p(1)(0).character.id)
        }
        assertCommandSuccess {
          GameStateValidator()(damagedGameState)
            .validateAbilityUseOnCharacter(
              s.p(0)(0).character.owner.id,
              castlingAbilityId,
              s.p(1)(0).character.id,
              UseData(s.p(0)(0).character.id),
            )
        }
        val checkUsedGameState = damagedGameState.useAbilityOnCharacter(checkAbilityId, s.p(1)(0).character.id)
        checkUsedGameState.abilityStates(checkAbilityId).cooldown should be(0)

        val castlingUsedGameState = damagedGameState.useAbilityOnCharacter(
          castlingAbilityId,
          s.p(1)(0).character.id,
          UseData(s.p(0)(0).character.id),
        )
        castlingUsedGameState.abilityStates(castlingAbilityId).cooldown should be(0)
      }
      "free ability was on CD" in {
        val cdGameState = damagedGameState
          .putAbilityOnCooldown(checkAbilityId)
          .putAbilityOnCooldown(castlingAbilityId)
        val checkCd = cdGameState.abilityStates(checkAbilityId).cooldown
        val castlingCd = cdGameState.abilityStates(castlingAbilityId).cooldown
        assertCommandSuccess {
          GameStateValidator()(cdGameState)
            .validateAbilityUseOnCharacter(s.p(0)(0).character.owner.id, checkAbilityId, s.p(1)(0).character.id)
        }
        assertCommandSuccess {
          GameStateValidator()(cdGameState)
            .validateAbilityUseOnCharacter(
              s.p(0)(0).character.owner.id,
              castlingAbilityId,
              s.p(1)(0).character.id,
              UseData(s.p(0)(0).character.id),
            )
        }
        val checkUsedGameState = cdGameState.useAbilityOnCharacter(checkAbilityId, s.p(1)(0).character.id)
        checkUsedGameState.abilityStates(checkAbilityId).cooldown should be(checkCd)

        val castlingUsedGameState =
          cdGameState.useAbilityOnCharacter(castlingAbilityId, s.p(1)(0).character.id, UseData(s.p(0)(0).character.id))
        castlingUsedGameState.abilityStates(castlingAbilityId).cooldown should be(castlingCd)
      }
    }
    "not be able to use free ability" when {
      "damage was dealt in multiple turns" in {
        val cdGameState = gameState.putAbilityOnCooldownOrDecrementFreeAbility(checkAbilityId)

        val multipleTurnDamageGameState = cdGameState
          .damageCharacter(
            s.p(0)(0).character.id,
            Damage(DamageType.True, (s.p(0)(0).character.state.maxHealthPoints * 0.3).toInt),
          )(random, gameState.id)
          .passTurn(s.p(0)(0).character.id)
          .passTurn(s.p(1)(0).character.id)
          .damageCharacter(
            s.p(0)(0).character.id,
            Damage(DamageType.True, (s.p(0)(0).character.state.maxHealthPoints * 0.3).toInt),
          )(random, gameState.id)
        assertCommandFailure {
          GameStateValidator()(multipleTurnDamageGameState)
            .validateAbilityUseOnCharacter(s.p(0)(0).character.owner.id, checkAbilityId, s.p(1)(0).character.id)
        }
      }
    }
    "remember ability cooldown before using" in {
      val cdGameState = damagedGameState.putAbilityOnCooldown(checkAbilityId).decrementAbilityCooldown(checkAbilityId)
      val checkCd = cdGameState.abilityStates(checkAbilityId).cooldown
      val checkUsedGameState = cdGameState.useAbilityOnCharacter(checkAbilityId, s.p(1)(0).character.id)
      checkUsedGameState.abilityStates(checkAbilityId).cooldown should be(checkCd)
    }
  }
}
