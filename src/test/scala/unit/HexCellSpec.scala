package unit

import com.tosware.nkm._
import com.tosware.nkm.models.game.GameState
import com.tosware.nkm.models.game.character.CharacterMetadata
import com.tosware.nkm.models.game.hex._
import helpers.{TestUtils, scenarios}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class HexCellSpec
  extends AnyWordSpecLike
    with Matchers
    with Logging
    with TestUtils
{
  private val metadata = CharacterMetadata.empty()
  private val s = scenarios.Simple2v2TestScenario(metadata)
  implicit val gameState: GameState = s.gameState
  implicit val hexMap: HexMap = s.gameState.hexMap
  "HexCell" must {
    "calculate correct neighbours" in {
      HexCoordinates(0, 0).toCell.getNeighbour(HexDirection.W).get.coordinates should be (HexCoordinates(-1, 0))
      HexCoordinates(-2, 2).toCell.getNeighbour(HexDirection.NE) should be (None) // not on map
      HexCoordinates(0, 0).toCell.getNeighbour(HexDirection.SW).get.coordinates should be (HexCoordinates(0, -1))
      HexCoordinates(0, 0).toCell.getNeighbour(HexDirection.SE).get.coordinates should be (HexCoordinates(1, -1))
    }
    "calculate correct line" in {
      HexCoordinates(0, 0).toCell.getLine(HexDirection.E, 0) should be (Set.empty)
      HexCoordinates(-2, 2).toCell.getLine(HexDirection.SE, 3).toCoords should be (CoordinateSet((-1, 1), (0, 0), (1, -1)))
    }

    "calculate correct lines" in {
      HexCoordinates(0, 0).toCell.getLines(Set(), 2) should be (Set.empty)
      HexCoordinates(0, 0).toCell.getLines(Set(HexDirection.W, HexDirection.SW), 0) should be (Set.empty)
      HexCoordinates(0, 0).toCell.getLines(Set(HexDirection.W, HexDirection.SW), 1).toCoords should be (CoordinateSet((-1, 0), (0, -1)))
      HexCoordinates(0, 0).toCell.getLines(Set(HexDirection.W, HexDirection.SW), 2).toCoords should be (CoordinateSet((-1, 0), (0, -1), (-2, 0))) // without one element outside map
    }

    "calculate correct areas" in {
      HexCoordinates(0, 0).toCell.getArea(0).toCoords should be (Set(HexCoordinates(0, 0)))
      HexCoordinates(0, 0).toCell.getArea(10).toCoords should be (hexMap.cells.toCoords)
      HexCoordinates(0, 0).toCell.getArea(10, Set(SearchFlag.StopAtWalls)).toCoords should be (
        hexMap.cells.filterNot(_.cellType == HexCellType.Wall).toCoords
      )
      HexCoordinates(0, 0).toCell.getArea(
        10,
        Set(SearchFlag.StopAtEnemies),
        Some(s.p(0)(0).character.owner.id),
      ).toCoords should be (
        hexMap.cells.toCoords -- hexMap.cells.whereEnemiesOfC(s.p(0)(0).character.id).toCoords
      )
    }
  }
}
