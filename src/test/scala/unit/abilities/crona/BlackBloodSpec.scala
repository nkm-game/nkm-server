package unit.abilities.crona

import com.tosware.nkm.models.game.abilities.crona.BlackBlood
import com.tosware.nkm.models.game.character.CharacterMetadata
import com.tosware.nkm.models.game.event.GameEvent
import com.tosware.nkm.models.game.game_state.GameState
import com.tosware.nkm.models.game.hex.TestHexMapName
import helpers.{TestScenario, TestUtils}

class BlackBloodSpec extends TestUtils {
  private val abilityMetadata = BlackBlood.metadata
  private val characterMetadata: Seq[CharacterMetadata] =
    Seq(
      CharacterMetadata.empty().copy(initialAbilitiesMetadataIds = Seq(abilityMetadata.id)),
      CharacterMetadata.empty(),
    )

  private val s = TestScenario.generateFromSeq(TestHexMapName.Simple1v9Line, characterMetadata)
  private val gameState: GameState = s.gameState.passTurn(s.defaultCharacter.id)
  private val abilityId = s.defaultAbilityId
  private val abilityRange = abilityMetadata.variables("radius")
  private val attackingCharacter = s.defaultEnemy

  abilityMetadata.name must {
    "deal damage to surrounding enemies" in {
      val newGameState: GameState = gameState.basicAttack(attackingCharacter.id, s.defaultCharacter.id)
      newGameState.gameLog.events
        .ofType[GameEvent.CharacterDamaged]
        .causedBy(abilityId).size shouldBe abilityRange

      val coordsInRange = s.defaultCharacter.parentCellOpt(newGameState).get.coordinates.getCircle(abilityRange)

      s.p(1).map(_.character).foreach { c =>
        val state = newGameState.characterById(c.id).state
        val hp = state.healthPoints
        val maxHp = state.maxHealthPoints
        if (coordsInRange.contains(c.parentCellOpt(newGameState).get.coordinates)) {
          hp should be < maxHp
        } else {
          hp should be(maxHp)
        }
      }
    }
  }
}
