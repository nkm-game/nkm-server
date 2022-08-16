package unit.abilities.sinon

import com.tosware.NKM.models.GameStateValidator
import com.tosware.NKM.models.game._
import com.tosware.NKM.models.game.abilities.sinon.PreciseShot
import com.tosware.NKM.models.game.hex.HexCoordinates
import com.tosware.NKM.providers.HexMapProvider.TestHexMapName
import helpers.TestUtils
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import com.tosware.NKM.models.game.hex.HexUtils._

class PreciseShotSpec
  extends AnyWordSpecLike
    with Matchers
    with TestUtils
{
  val metadata = CharacterMetadata.empty().copy(initialAbilitiesMetadataIds = Seq(PreciseShot.metadata.id))
  implicit val gameState = getTestGameState(TestHexMapName.Simple2v2, Seq(
    Seq(metadata.copy(name = "Empty1"), metadata.copy(name = "Empty2")),
    Seq(metadata.copy(name = "Empty3"), metadata.copy(name = "Empty4")),
  ))

  val p0FirstCharacter = characterOnPoint(HexCoordinates(0, 0))
  val p0SecondCharacter = characterOnPoint(HexCoordinates(-1, 0))

  val p1FirstCharacter = characterOnPoint(HexCoordinates(3, 0))
  val p1SecondCharacter = characterOnPoint(HexCoordinates(4, 0))

  val abilityId = p0FirstCharacter.state.abilities.head.id

  PreciseShot.metadata.name must {
    "be able to deal damage" in {
      val r = GameStateValidator()
        .validateAbilityUseOnCharacter(p0FirstCharacter.owner.id, abilityId, p1FirstCharacter.id)
      assertCommandSuccess(r)

      val newGameState: GameState = gameState.useAbilityOnCharacter(abilityId, p1FirstCharacter.id)
      newGameState.gameLog.events.ofType[GameEvent.CharacterDamaged].exists(_.causedById == abilityId)
    }
  }
}