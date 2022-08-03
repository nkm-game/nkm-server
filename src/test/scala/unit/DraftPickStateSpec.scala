package unit

import com.tosware.NKM.models.game.CharacterMetadata.CharacterMetadataId
import com.tosware.NKM.models.game.Player.PlayerId
import com.tosware.NKM.models.game.draftpick._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import scala.annotation.tailrec

class DraftPickStateSpec
  extends AnyWordSpecLike
    with Matchers {
  "Draft Pick" must {
//    "validate bans" in {
//      fail()
//    }
//    "validate picks" in {
//      fail()
//    }
    "calculate proper current player picking" in {
      val numberOfPlayersSeq = 1 to 6
      val numberOfCharactersPerPlayerSeq = 1 to 6

      def test(numberOfPlayers: Int, numberOfCharactersPerPlayer: Int) = {
        val config = DraftPickConfig(
          playersPicking = (1 to numberOfPlayers).map(x => s"player $x"),
          availableCharacters = (1 to 40).map(x => s"id $x").toSet,
          numberOfBansPerPlayer = 0,
          numberOfCharactersPerPlayer = numberOfCharactersPerPlayer,
        )
        var state = DraftPickState.empty(config)
        state.pickPhase shouldBe DraftPickPhase.Picking

        def validateAndPick(playerId: PlayerId, character: CharacterMetadataId): Unit = {
          state.validatePick(playerId, character) shouldBe true
          state = state.pick(playerId, character)
        }


        @tailrec
        def pickUntilEnd(pickOrder: Range): Unit = {
          for (i <- pickOrder) {
            state.currentPlayerPicking shouldBe Some(config.playersPicking(i))
            validateAndPick(config.playersPicking(i), state.charactersAvailableToPick.head)
          }
          if (state.pickPhase != DraftPickPhase.Finished) pickUntilEnd(pickOrder.reverse)
        }

        pickUntilEnd(0 until numberOfPlayers)

        state.currentPlayerPicking shouldBe None

      }

      numberOfPlayersSeq.foreach { p =>
        numberOfCharactersPerPlayerSeq.foreach { c =>
          test(p, c)
        }
      }
    }
  }
}
