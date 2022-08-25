package unit.abilities.hecate

import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.abilities.hecate.Aster
import com.tosware.nkm.models.game.hex.HexCoordinates
import com.tosware.nkm.models.game.hex.HexUtils._
import com.tosware.nkm.providers.HexMapProvider.TestHexMapName
import helpers.TestUtils
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class AsterSpec
  extends AnyWordSpecLike
    with Matchers
    with TestUtils
{
  val metadata = CharacterMetadata.empty().copy(initialAbilitiesMetadataIds = Seq(Aster.metadata.id))
  implicit val gameState = getTestGameState(TestHexMapName.Simple2v2, Seq(
    Seq(metadata.copy(name = "Empty1"), metadata.copy(name = "Empty2")),
    Seq(metadata.copy(name = "Empty3"), metadata.copy(name = "Empty4")),
  ))

  val p0FirstCharacterSpawnCoordinates = HexCoordinates(0, 0)
  val p0SecondCharacterSpawnCoordinates = HexCoordinates(-1, 0)
  val p1FirstCharacterSpawnCoordinates = HexCoordinates(3, 0)
  val p1SecondCharacterSpawnCoordinates = HexCoordinates(4, 0)

  val p0FirstCharacter = characterOnPoint(p0FirstCharacterSpawnCoordinates)
  val p0SecondCharacter = characterOnPoint(p0SecondCharacterSpawnCoordinates)

  val p1FirstCharacter = characterOnPoint(p1FirstCharacterSpawnCoordinates)
  val p1SecondCharacter = characterOnPoint(p1SecondCharacterSpawnCoordinates)

  val abilityId = p0FirstCharacter.state.abilities.head.id

  Aster.metadata.name must {
    "be able to damage characters" in {
      val validator = GameStateValidator()

      val allCoords = gameState.hexMap.get.cells.toCoords
      allCoords.foreach { c =>
        val r = validator.validateAbilityUseOnCoordinates(p0FirstCharacter.owner.id, abilityId, c)
        assertCommandSuccess(r)
      }

      val abilityUsedGameState: GameState = gameState.useAbilityOnCoordinates(abilityId, p0SecondCharacterSpawnCoordinates)
      abilityUsedGameState.gameLog.events
        .ofType[GameEvent.CharacterDamaged]
        .causedBy(abilityId)
        .size should be (2)
    }
  }
}