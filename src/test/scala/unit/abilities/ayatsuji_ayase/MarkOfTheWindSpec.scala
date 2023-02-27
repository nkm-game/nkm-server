package unit.abilities.ayatsuji_ayase

import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.abilities.ayatsuji_ayase.{CrackTheSky, MarkOfTheWind}
import com.tosware.nkm.models.game.character.CharacterMetadata
import com.tosware.nkm.models.game.hex.HexCoordinates
import helpers.{TestUtils, scenarios}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class MarkOfTheWindSpec
  extends AnyWordSpecLike
    with Matchers
    with TestUtils
{
  private val metadata = CharacterMetadata.empty()
    .copy(initialAbilitiesMetadataIds = Seq(
      MarkOfTheWind.metadata.id,
      CrackTheSky.metadata.id,
    ))
  private val s = scenarios.Simple1v1TestScenario(metadata)
  private implicit val gameState: GameState = s.gameState
  private val markAbilityId =
    s.characters.p0.state.abilities(0).id
  private val crackAbilityId =
    s.characters.p0.state.abilities(1).id

  MarkOfTheWind.metadata.name must {
    "be able to set up traps" in {
      assertCommandSuccess {
        GameStateValidator()(gameState)
          .validateAbilityUseOnCoordinates(s.characters.p0.owner.id, markAbilityId, HexCoordinates(0, 0))
      }
    }

    "be able to detonate selected traps" in {
      fail()
      val ngs: GameState = gameState.useAbility(markAbilityId, ???)
      val ngs: GameState = gameState.useAbility(crackAbilityId, ???)
    }

    "delete first trap if set above the limit" in {
      fail()
      val ngs: GameState = gameState.useAbility(markAbilityId, ???)
    }
  }
}