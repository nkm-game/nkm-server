package unit.abilities.sinon

import com.tosware.NKM.models.GameStateValidator
import com.tosware.NKM.models.game._
import com.tosware.NKM.models.game.abilities.sinon.TacticalEscape
import com.tosware.NKM.models.game.hex.HexCoordinates
import com.tosware.NKM.providers.HexMapProvider.TestHexMapName
import helpers.TestUtils
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class TacticalEscapeSpec
  extends AnyWordSpecLike
    with Matchers
    with TestUtils
{
  val metadata = CharacterMetadata.empty().copy(initialAbilitiesMetadataIds = Seq(TacticalEscape.metadata.id))
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

  TacticalEscape.metadata.name must {
    "be able to modify character speed" in {
      val validator = GameStateValidator()
      val r = validator.validateAbilityUseWithoutTarget(p0FirstCharacter.owner.id, abilityId)
      assertCommandSuccess(r)

      val abilityUsedGameState: GameState = gameState.useAbilityWithoutTarget(abilityId)
      p0FirstCharacter.state.speed should be < abilityUsedGameState.characterById(p0FirstCharacter.id).get.state.speed
    }
  }
}