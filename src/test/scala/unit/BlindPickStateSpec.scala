package unit

import com.tosware.NKM.models.game.CharacterMetadata.CharacterMetadataId
import com.tosware.NKM.models.game.Player.PlayerId
import com.tosware.NKM.models.game.blindpick._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class BlindPickStateSpec
  extends AnyWordSpecLike
    with Matchers {
  "Blind Pick" must {
    "work" in {
      val numberOfPlayersSeq = 1 to 6
      val numberOfCharactersPerPlayerSeq = 1 to 6

      def test(numberOfPlayers: Int, numberOfCharactersPerPlayer: Int) = {
        val config = BlindPickConfig(
          playersPicking = (1 to numberOfPlayers).map(x => s"player $x"),
          availableCharacters = (1 to 40).map(x => s"id $x").toSet,
          numberOfCharactersPerPlayer = numberOfCharactersPerPlayer,
        )
        var state = BlindPickState.empty(config)
        state.pickPhase shouldBe BlindPickPhase.Picking

        def validateAndPick(playerId: PlayerId, characters: Set[CharacterMetadataId]): Unit = {
          state.validatePick(playerId, characters) shouldBe true
          state = state.pick(playerId, characters)
        }

        for (i <- 0 until numberOfPlayers) {
          validateAndPick(config.playersPicking(i), state.config.availableCharacters.take(numberOfCharactersPerPlayer))
        }

        state.pickPhase shouldBe BlindPickPhase.Finished

      }

      numberOfPlayersSeq.foreach { p =>
        numberOfCharactersPerPlayerSeq.foreach { c =>
          test(p, c)
        }
      }
    }
  }
}
