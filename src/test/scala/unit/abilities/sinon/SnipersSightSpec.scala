package unit.abilities.sinon

import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.abilities.sinon.SnipersSight
import com.tosware.nkm.models.game.hex.HexCoordinates
import com.tosware.nkm.providers.HexMapProvider.TestHexMapName
import helpers.TestUtils
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class SnipersSightSpec
  extends AnyWordSpecLike
    with Matchers
    with TestUtils
{
  val metadata = CharacterMetadata.empty().copy(initialAbilitiesMetadataIds = Seq(SnipersSight.metadata.id))
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

  SnipersSight.metadata.name must {
    "be able to attack characters in radial range" in {
      val newGameState = gameState.teleportCharacter(p0FirstCharacter.id, HexCoordinates(0, 1))(random, gameState.id)
      val r = GameStateValidator()(newGameState).validateBasicAttackCharacter(p0FirstCharacter.owner.id, p0FirstCharacter.id, p1SecondCharacter.id)
      assertCommandSuccess(r)
    }
  }
}