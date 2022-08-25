package unit.abilities.aqua

import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.abilities.aqua.Purification
import com.tosware.nkm.models.game.effects._
import com.tosware.nkm.models.game.hex.{HexCoordinates, NkmUtils}
import com.tosware.nkm.providers.HexMapProvider.TestHexMapName
import helpers.TestUtils
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class PurificationSpec
  extends AnyWordSpecLike
    with Matchers
    with TestUtils
{
  val metadata = CharacterMetadata.empty().copy(initialAbilitiesMetadataIds = Seq(Purification.metadata.id))
  implicit val gameState = getTestGameState(TestHexMapName.Simple2v2, Seq(
    Seq(metadata.copy(name = "Empty1"), metadata.copy(name = "Empty2")),
    Seq(metadata.copy(name = "Empty3"), metadata.copy(name = "Empty4")),
  ))

  val p0FirstCharacter = characterOnPoint(HexCoordinates(0, 0))
  val p0SecondCharacter = characterOnPoint(HexCoordinates(-1, 0))

  val p1FirstCharacter = characterOnPoint(HexCoordinates(3, 0))
  val p1SecondCharacter = characterOnPoint(HexCoordinates(4, 0))

  Purification.metadata.name must {
    "be able to remove negative effects" in {
      val effectGameState = gameState
        .addEffect(p0SecondCharacter.id, DisarmEffect(NkmUtils.randomUUID(), 5))(random, gameState.id)
        .addEffect(p0SecondCharacter.id, StunEffect(NkmUtils.randomUUID(), 5))(random, gameState.id)
        .addEffect(p0SecondCharacter.id, GroundEffect(NkmUtils.randomUUID(), 5))(random, gameState.id)
        .addEffect(p0SecondCharacter.id, SnareEffect(NkmUtils.randomUUID(), 5))(random, gameState.id)

      val abilityId = p0FirstCharacter.state.abilities.head.id

      val r = GameStateValidator()(effectGameState)
        .validateAbilityUseOnCharacter(p0FirstCharacter.owner.id, abilityId, p0SecondCharacter.id, UseData())
      assertCommandSuccess(r)

      val purifiedGameState: GameState = effectGameState.useAbilityOnCharacter(abilityId, p0SecondCharacter.id, UseData())
      purifiedGameState.characterById(p0SecondCharacter.id).get.state.effects should be (Seq.empty)
    }

    "not be able to use if target has no negative effects" in {
      val abilityId = p0FirstCharacter.state.abilities.head.id
      val r = GameStateValidator()
        .validateAbilityUseOnCharacter(p0SecondCharacter.owner.id ,abilityId, p0SecondCharacter.id, UseData())
      assertCommandFailure(r)
    }
  }
}