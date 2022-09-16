package unit.abilities.blank

import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.abilities.blank.{AceInTheHole, Castling, Check}
import helpers.{TestUtils, scenarios}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class AceInTheHoleSpec
  extends AnyWordSpecLike
    with Matchers
    with TestUtils
{
  private val abilityMetadata = AceInTheHole.metadata
  private val characterMetadata = CharacterMetadata.empty()
    .copy(initialAbilitiesMetadataIds = Seq(
      abilityMetadata.id,
      Check.metadata.id,
      Castling.metadata.id,
    ))
  private val s = scenarios.Simple1v9LineTestScenario(characterMetadata)
  private implicit val gameState: GameState = s.gameState
  private val abilityId = s.characters.p0.state.abilities.head.id

  abilityMetadata.name must {
    "be able to use any free ability" when {
      "free ability was not on CD" in {
        fail()
      }
      "free ability was on CD" in {
        fail()
      }
    }
    "not be able to use free ability" when {
      "damage was dealt in multiple turns" in {
        fail()
      }
      "free ability has no targets" in {
        fail()
      }
    }
    "remember ability cooldown before using" in {
      fail()
    }
  }
}