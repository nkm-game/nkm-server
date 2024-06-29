package unit

import com.tosware.nkm.models.game.{GameMode, VictoryStatus}
import com.tosware.nkm.models.game.character.CharacterMetadata
import com.tosware.nkm.models.game.game_state.GameState
import com.tosware.nkm.models.game.hex.TestHexMapName
import helpers.*

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.DurationInt
import scala.concurrent.ExecutionContext.Implicits.global

class CaptureThePointSpec extends TestUtils {
  private val s =
    TestScenario.generate(TestHexMapName.Simple2v2Points, CharacterMetadata.empty(), GameMode.CaptureThePoint)
  private val pointGroups = s.gameState.hexMap.pointGroups
  private val point0Coords = pointGroups(0).coordinates.toSeq
  private val point1Coords = pointGroups(1).coordinates.toSeq
  private val point2Coords = pointGroups(2).coordinates.toSeq

  private val oneToZeroGs = s.gameState
    .teleportCharacter(s.defaultCharacter.id, point0Coords(0))
  private val twoToOneGs = s.gameState
    .teleportCharacter(s.defaultCharacter.id, point0Coords(0))
    .teleportCharacter(s.p(0)(1).character.id, point0Coords(1))
    .teleportCharacter(s.defaultEnemy.id, point0Coords(2))

  private val zeroToOneGs = s.gameState
    .teleportCharacter(s.defaultEnemy.id, point0Coords(0))
  private val oneToTwoGs = s.gameState
    .teleportCharacter(s.defaultCharacter.id, point0Coords(0))
    .teleportCharacter(s.p(1)(1).character.id, point0Coords(1))
    .teleportCharacter(s.defaultEnemy.id, point0Coords(2))

  private val willBeADrawGs = s.gameState
    .teleportCharacter(s.defaultCharacter.id, point0Coords(0))
    .teleportCharacter(s.defaultEnemy.id, point2Coords(0))

  "capture the point" must {
    "initialize hex point group ownerships" in {
      s.gameState.hexPointGroupOwnerships.size should be(3)
      s.gameState.hexPointGroupOwnerships(pointGroups(0).id) should be(None)
      s.gameState.hexPointGroupOwnerships(pointGroups(1).id) should be(None)
      s.gameState.hexPointGroupOwnerships(pointGroups(2).id) should be(None)
    }
    "initialize player points" in {
      s.gameState.players(0).points should be(0)
      s.gameState.players(1).points should be(0)
    }
    "give points to players at the end of a phase" in {
      {
        val ngs = oneToZeroGs.skipPhase()
        ngs.players(0).points should be(5)
        ngs.players(1).points should be(0)
      }
      {
        val ngs = oneToZeroGs.skipPhase().skipPhase()
        ngs.players(0).points should be(10)
        ngs.players(1).points should be(0)
      }
      {
        val ngs = s.gameState
          .teleportCharacter(s.defaultCharacter.id, point0Coords(0))
          .teleportCharacter(s.defaultEnemy.id, point1Coords(0))
          .skipPhase()
          .skipPhase()
        ngs.players(0).points should be(10)
        ngs.players(1).points should be(6)
      }
    }
    "end the game when the point threshold is met" when {
      "p0 win" in {
        val ngs =
          Await.result(
            Future(oneToZeroGs.skipPhaseWhile { gs =>
              gs.players(0).points <= 50
            }),
            5.seconds,
          )
        ngs.players(0).victoryStatus should be(VictoryStatus.Won)
        ngs.players(1).victoryStatus should be(VictoryStatus.Lost)
      }
      "p1 win" in {
        val ngs = Await.result(
          Future(oneToTwoGs.skipPhaseWhile(gs => gs.players(1).points <= 50)),
          5.seconds,
        )
        ngs.players(0).victoryStatus should be(VictoryStatus.Lost)
        ngs.players(1).victoryStatus should be(VictoryStatus.Won)
      }
      "draw" in {
        val ngs = Await.result(
          Future(willBeADrawGs.skipPhaseWhile(gs => gs.players(1).points <= 50)),
          5.seconds,
        )
        ngs.players(0).victoryStatus should be(VictoryStatus.Drawn)
        ngs.players(1).victoryStatus should be(VictoryStatus.Drawn)
      }
    }
  }

  "a hex point group" must {
    "change the owner" when {
      "there are more friendly characters than enemies" when {
        "the point is not taken" in {
          def test(gs: GameState) = {
            val ngs = gs.skipPhase()
            ngs.hexPointGroupOwnerships(pointGroups(0).id) should be(Some(s.owners(0)))
          }
          test(oneToZeroGs)
          test(twoToOneGs)
        }
        "the point is taken by enemies" in {
          def test(gs: GameState) = {
            val takenGs = gs
              .setHexPointGroupOwnership(pointGroups(0).id, Some(s.owners(1)))

            takenGs.hexPointGroupOwnerships(pointGroups(0).id) should be(Some(s.owners(1)))

            val ngs = takenGs.skipPhase()

            ngs.hexPointGroupOwnerships(pointGroups(0).id) should be(Some(s.owners(0)))
          }
          test(oneToZeroGs)
          test(twoToOneGs)
        }
      }
      "there are more enemies than friendly characters" when {
        "the point is not taken" in {
          def test(gs: GameState) = {
            val ngs = gs.skipPhase()
            ngs.hexPointGroupOwnerships(pointGroups(0).id) should be(Some(s.owners(1)))
          }
          test(zeroToOneGs)
          test(oneToTwoGs)
        }
        "the point is taken by friends" in {
          def test(gs: GameState) = {
            val takenGs = gs
              .setHexPointGroupOwnership(pointGroups(0).id, Some(s.owners(0)))

            takenGs.hexPointGroupOwnerships(pointGroups(0).id) should be(Some(s.owners(0)))

            val ngs = takenGs.skipPhase()

            ngs.hexPointGroupOwnerships(pointGroups(0).id) should be(Some(s.owners(1)))
          }
          test(zeroToOneGs)
          test(oneToTwoGs)
        }
      }
    }
    "not change the owner" when {
      "there is no one on the point" in {
        {
          val ngs = s.gameState.skipPhase()
          ngs.hexPointGroupOwnerships(pointGroups(0).id) should be(None)
        }

        {
          val ngs = s.gameState
            .setHexPointGroupOwnership(pointGroups(0).id, Some(s.owners(0)))
            .skipPhase()
          ngs.hexPointGroupOwnerships(pointGroups(0).id) should be(Some(s.owners(0)))
        }
      }
      "there are more friendly characters than enemies" when {
        "the point is taken by friends" in {
          def test(gs: GameState) = {
            val ngs = gs
              .setHexPointGroupOwnership(pointGroups(0).id, Some(s.owners(0)))
              .skipPhase()
            ngs.hexPointGroupOwnerships(pointGroups(0).id) should be(Some(s.owners(0)))
          }
          test(oneToZeroGs)
          test(twoToOneGs)
        }
      }
      "there are more enemies than friendly characters" when {
        "the point is taken by enemies" in {
          def test(gs: GameState) = {
            val ngs = gs
              .setHexPointGroupOwnership(pointGroups(0).id, Some(s.owners(1)))
              .skipPhase()
            ngs.hexPointGroupOwnerships(pointGroups(0).id) should be(Some(s.owners(1)))
          }
          test(zeroToOneGs)
          test(oneToTwoGs)
        }
      }
    }
  }
}
