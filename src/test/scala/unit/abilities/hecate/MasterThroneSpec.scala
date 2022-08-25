package unit.abilities.hecate

import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.abilities.hecate.{Aster, MasterThrone}
import com.tosware.nkm.models.game.hex.HexCoordinates
import com.tosware.nkm.providers.HexMapProvider.TestHexMapName
import helpers.TestUtils
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class MasterThroneSpec
  extends AnyWordSpecLike
    with Matchers
    with TestUtils
{
  val metadata = CharacterMetadata.empty().copy(initialAbilitiesMetadataIds = Seq(MasterThrone.metadata.id, Aster.metadata.id))
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
  val asterAbilityId = p0FirstCharacter.state.abilities.tail.head.id

  MasterThrone.metadata.name must {
    "not be initialized with energy" in {
      gameState.abilityById(abilityId).get.asInstanceOf[MasterThrone].collectedEnergy should be(0)
    }

    "be able to collect energy from basic attacks" in {
      val newGameState: GameState = gameState.basicAttack(p0FirstCharacter.id, p1FirstCharacter.id)
      newGameState.abilityById(abilityId).get.asInstanceOf[MasterThrone].collectedEnergy should be > 0
    }

    "be able to collect energy from normal ability" in {
      val newGameState: GameState = gameState.useAbilityOnCoordinates(asterAbilityId, p0SecondCharacterSpawnCoordinates)
      newGameState.abilityById(abilityId).get.asInstanceOf[MasterThrone].collectedEnergy should be > 0
    }
  }
}