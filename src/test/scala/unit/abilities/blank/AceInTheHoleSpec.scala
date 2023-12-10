package unit.abilities.blank

import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.abilities.blank.*
import com.tosware.nkm.models.game.ability.UseData
import com.tosware.nkm.models.game.character.CharacterMetadata
import com.tosware.nkm.models.game.hex.TestHexMapName
import helpers.{TestScenario, TestUtils}
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
  private val s = TestScenario.generate(TestHexMapName.Simple1v1, characterMetadata)
  implicit private val gameState: GameState = s.gameState
  private val damagedGameState = gameState.damageCharacter(
    s.defaultCharacter.id,
    Damage(DamageType.True, (s.defaultCharacter.state.maxHealthPoints * 0.5).toInt),
  )(random, gameState.id)
  private val checkAbilityId = s.defaultCharacter.state.abilities(1).id
  private val castlingAbilityId = s.defaultCharacter.state.abilities(2).id

  abilityMetadata.name must {
    "be able to use any free ability" when {
      "free ability was not on CD" in {
        assertCommandSuccess {
          GameStateValidator()(damagedGameState)
            .validateAbilityUse(s.owners(0), checkAbilityId, UseData(s.defaultEnemy.id))
        }
        assertCommandSuccess {
          GameStateValidator()(damagedGameState)
            .validateAbilityUse(
              s.owners(0),
              castlingAbilityId,
              UseData(Seq(s.defaultEnemy.id, s.defaultCharacter.id)),
            )
        }
        val checkUsedGameState = damagedGameState.useAbility(checkAbilityId, UseData(s.defaultEnemy.id))
        checkUsedGameState.abilityStates(checkAbilityId).cooldown should be(0)

        val castlingUsedGameState = damagedGameState.useAbility(
          castlingAbilityId,
          UseData(Seq(s.defaultEnemy.id, s.defaultCharacter.id)),
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
            .validateAbilityUse(s.owners(0), checkAbilityId, UseData(s.defaultEnemy.id))
        }
        assertCommandSuccess {
          GameStateValidator()(cdGameState)
            .validateAbilityUse(
              s.owners(0),
              castlingAbilityId,
              UseData(Seq(s.defaultEnemy.id, s.defaultCharacter.id)),
            )
        }
        val checkUsedGameState = cdGameState.useAbility(checkAbilityId, UseData(s.defaultEnemy.id))
        checkUsedGameState.abilityStates(checkAbilityId).cooldown should be(checkCd)

        val castlingUsedGameState =
          cdGameState.useAbility(castlingAbilityId, UseData(Seq(s.defaultEnemy.id, s.defaultCharacter.id)))
        castlingUsedGameState.abilityStates(castlingAbilityId).cooldown should be(castlingCd)
      }
    }
    "not be able to use free ability" when {
      "damage was dealt in multiple turns" in {
        val cdGameState = gameState.putAbilityOnCooldownOrDecrementFreeAbility(checkAbilityId)

        val multipleTurnDamageGameState = cdGameState
          .damageCharacter(
            s.defaultCharacter.id,
            Damage(DamageType.True, (s.defaultCharacter.state.maxHealthPoints * 0.3).toInt),
          )(random, gameState.id)
          .passTurn(s.defaultCharacter.id)
          .passTurn(s.defaultEnemy.id)
          .damageCharacter(
            s.defaultCharacter.id,
            Damage(DamageType.True, (s.defaultCharacter.state.maxHealthPoints * 0.3).toInt),
          )(random, gameState.id)
        assertCommandFailure {
          GameStateValidator()(multipleTurnDamageGameState)
            .validateAbilityUse(s.owners(0), checkAbilityId, UseData(s.defaultEnemy.id))
        }
      }
    }
    "remember ability cooldown before using" in {
      val cdGameState = damagedGameState.putAbilityOnCooldown(checkAbilityId).decrementAbilityCooldown(checkAbilityId)
      val checkCd = cdGameState.abilityStates(checkAbilityId).cooldown
      val checkUsedGameState = cdGameState.useAbility(checkAbilityId, UseData(s.defaultEnemy.id))
      checkUsedGameState.abilityStates(checkAbilityId).cooldown should be(checkCd)
    }
  }
}
