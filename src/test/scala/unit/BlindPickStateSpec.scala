package unit

import com.tosware.nkm.*
import com.tosware.nkm.models.UseCheck
import com.tosware.nkm.models.game.pick.blindpick.*
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
          UseCheck.canBeUsed(state.pickChecks(playerId, config.availableCharacters)).toBoolean shouldBe false
          UseCheck.canBeUsed(state.pickChecks(playerId, Set())).toBoolean shouldBe false

          UseCheck.canBeUsed(state.pickChecks(playerId, characters)).toBoolean shouldBe true
          state = state.pick(playerId, characters)
        }

        for (i <- 0 until numberOfPlayers)
          validateAndPick(config.playersPicking(i), state.config.availableCharacters.take(numberOfCharactersPerPlayer))

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
