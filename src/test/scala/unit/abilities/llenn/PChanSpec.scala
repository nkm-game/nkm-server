package unit.abilities.llenn

import com.tosware.NKM.models.game._
import com.tosware.NKM.models.game.abilities.llenn.PChan
import com.tosware.NKM.models.game.hex.HexCoordinates
import com.tosware.NKM.models.{Damage, DamageType}
import com.tosware.NKM.providers.HexMapProvider.TestHexMapName
import helpers.TestUtils
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class PChanSpec
  extends AnyWordSpecLike
    with Matchers
    with TestUtils
{
  val metadata = CharacterMetadata.empty().copy(initialAbilitiesMetadataIds = Seq(PChan.metadata.id))
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

  PChan.metadata.name must {
    "increase movement speed on death of friend" in {
      val initialSpeed = metadata.initialSpeed
      val newGameState = gameState.damageCharacter(p1SecondCharacter.id, Damage(DamageType.True, 100))(gameState.id)
      newGameState.characterById(p0FirstCharacter.id).get.state.pureSpeed should be(initialSpeed)
      val newGameState2 = gameState.damageCharacter(p0SecondCharacter.id, Damage(DamageType.True, 100))(gameState.id)
      newGameState2.characterById(p0FirstCharacter.id).get.state.pureSpeed should be(initialSpeed + 2)
    }
  }
}