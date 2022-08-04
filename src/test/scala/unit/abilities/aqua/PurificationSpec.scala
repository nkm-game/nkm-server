package unit.abilities.aqua

import com.tosware.NKM.models.game._
import com.tosware.NKM.models.game.abilities.aqua.Purification
import com.tosware.NKM.models.game.effects._
import com.tosware.NKM.models.game.hex.HexCoordinates
import com.tosware.NKM.providers.HexMapProvider.TestHexMapName
import helpers.TestUtils
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class PurificationSpec
  extends AnyWordSpecLike
    with Matchers
    with TestUtils
{
  val metadata = CharacterMetadata.empty().copy(initialAbilitiesMetadataIds = Seq(Purification.metadata.id))
  val gameState = getTestGameState(TestHexMapName.Simple2v2, Seq(
    Seq(metadata.copy(name = "Empty1"), metadata.copy(name = "Empty2")),
    Seq(metadata.copy(name = "Empty3"), metadata.copy(name = "Empty4")),
  ))

  def characterIdOnPoint(hexCoordinates: HexCoordinates) = gameState.hexMap.get.getCell(hexCoordinates).get.characterId.get
  def characterOnPoint(hexCoordinates: HexCoordinates) = gameState.characterById(characterIdOnPoint(hexCoordinates)).get

  val p0FirstCharacter = characterOnPoint(HexCoordinates(0, 0))
  val p0SecondCharacter = characterOnPoint(HexCoordinates(-1, 0))

  val p1FirstCharacter = characterOnPoint(HexCoordinates(3, 0))
  val p1SecondCharacter = characterOnPoint(HexCoordinates(4, 0))

  Purification.metadata.name must {
    "be able to remove negative effects" in {
      val effectGameState = gameState
        .addEffect(p0SecondCharacter.id, DisarmEffect(5))
        .addEffect(p0SecondCharacter.id, StunEffect(5))
        .addEffect(p0SecondCharacter.id, GroundEffect(5))
        .addEffect(p0SecondCharacter.id, SnareEffect(5))

      val abilityId = p0FirstCharacter.state.abilities.head.id
      val purifiedGameState: GameState = effectGameState.useAbilityOnCharacter(abilityId, p0SecondCharacter.id, UseData())
      purifiedGameState.characterById(p0SecondCharacter.id).get.state.effects should be (Seq.empty)
    }
  }
}