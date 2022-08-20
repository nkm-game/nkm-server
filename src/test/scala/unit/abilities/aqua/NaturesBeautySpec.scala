package unit.abilities.aqua

import com.tosware.NKM.models.game._
import com.tosware.NKM.models.game.abilities.aqua.NaturesBeauty
import com.tosware.NKM.models.game.hex.HexCoordinates
import com.tosware.NKM.providers.HexMapProvider.TestHexMapName
import helpers.TestUtils
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class NaturesBeautySpec
  extends AnyWordSpecLike
    with Matchers
    with TestUtils
{
  val metadata = CharacterMetadata.empty().copy(initialAbilitiesMetadataIds = Seq(NaturesBeauty.metadata.id))
  implicit val gameState = getTestGameState(TestHexMapName.Simple2v2, Seq(
    Seq(metadata.copy(name = "Empty1"), metadata.copy(name = "Empty2")),
    Seq(metadata.copy(name = "Empty3"), metadata.copy(name = "Empty4")),
  ))

  val p0FirstCharacterId = characterIdOnPoint(HexCoordinates(0, 0))
  val p0SecondCharacterId = characterIdOnPoint(HexCoordinates(-1, 0))

  val p1FirstCharacterId = characterIdOnPoint(HexCoordinates(3, 0))
  val p1SecondCharacterId = characterIdOnPoint(HexCoordinates(4, 0))

  NaturesBeauty.metadata.name must {
    "be able to heal friends via basic attacks" in {
      val damagedGameState = gameState.setHp(p0SecondCharacterId, 30)(random, gameState.id)
      val healedGameState = damagedGameState.basicAttack(p0FirstCharacterId, p0SecondCharacterId)
      healedGameState.characterById(p0FirstCharacterId).get.isFriendForC(p0SecondCharacterId)(healedGameState) shouldBe true
      healedGameState.characterById(p0SecondCharacterId).get.state.healthPoints should be(30 + healedGameState.characterById(p0FirstCharacterId).get.state.attackPoints)
    }
  }
}