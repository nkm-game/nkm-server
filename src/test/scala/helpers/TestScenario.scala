package helpers

import com.tosware.nkm.{AbilityId, AbilityMetadataId, PlayerId}
import com.tosware.nkm.models.game.GameState
import com.tosware.nkm.models.game.character.{CharacterMetadata, NkmCharacter}
import com.tosware.nkm.models.game.hex.{HexCoordinates, TestHexMapName}

object TestScenario extends TestUtils {
  def generate(testHexMapName: TestHexMapName, metadata: CharacterMetadata = CharacterMetadata.empty()): TestScenario =
    new TestScenario {
      val gameState: GameState = getTestGameState(testHexMapName, metadata)
    }

  def generate(testHexMapName: TestHexMapName, abilityMetadataId: AbilityMetadataId): TestScenario =
    generate(testHexMapName, CharacterMetadata.withAbility(abilityMetadataId))
}

trait TestScenario extends TestUtils {
  val gameState: GameState
  lazy val p: Seq[Seq[TestCharacterData]] =
    bindPlayerData()(gameState)
  lazy val owners: Seq[PlayerId] =
    gameState.players.map(_.id)
  lazy val defaultCharacter: NkmCharacter =
    p(0)(0).character
  lazy val defaultEnemy: NkmCharacter =
    p(1)(0).character
  lazy val defaultAbilityId: AbilityId =
    defaultCharacter.state.abilities.head.id
  lazy val defaultCoordinates: HexCoordinates =
    defaultCharacter.parentCell(gameState).get.coordinates
  lazy val ultGs: GameState =
    gameState.incrementPhase(4)
}
