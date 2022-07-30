package unit

import com.tosware.NKM.models.game.hex.HexUtils.CoordinateSet
import com.tosware.NKM.models.game.hex.{HexCoordinates, HexDirection}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.slf4j.{Logger, LoggerFactory}


class HexCoordinatesSpec
  extends AnyWordSpecLike
    with Matchers {
  val logger: Logger = LoggerFactory.getLogger(getClass)
  "HexCoordinates" must {
    "calculate correct neighbours" in {
      HexCoordinates(0, 0).getNeighbour(HexDirection.W) should be (HexCoordinates(-1, 0))
      HexCoordinates(-2, 2).getNeighbour(HexDirection.NE) should be (HexCoordinates(-2, 3))
      HexCoordinates(0, -2).getNeighbour(HexDirection.NW) should be (HexCoordinates(-1, -1))
      HexCoordinates(0, 0).getNeighbour(HexDirection.SW) should be (HexCoordinates(0, -1))
      HexCoordinates(0, 0).getNeighbour(HexDirection.SE) should be (HexCoordinates(1, -1))
      HexCoordinates(0, -3).getNeighbour(HexDirection.E) should be (HexCoordinates(1, -3))
    }
    "calculate correct line" in {
      HexCoordinates(0, 0).getLine(HexDirection.E, 0) should be (Set.empty)
      HexCoordinates(-2, 2).getLine(HexDirection.SE, 3) should be (CoordinateSet((-1, 1), (0, 0), (1, -1)))
    }

    "calculate correct lines" in {
      HexCoordinates(0, 0).getLines(Set(), 2) should be (Set.empty)
      HexCoordinates(0, 0).getLines(Set(HexDirection.W, HexDirection.SW), 0) should be (Set.empty)
      HexCoordinates(0, 0).getLines(Set(HexDirection.W, HexDirection.SW), 1) should be (CoordinateSet((-1, 0), (0, -1)))
      HexCoordinates(0, 0).getLines(Set(HexDirection.W, HexDirection.SW), 2) should be (CoordinateSet((-1, 0), (0, -1), (-2, 0), (0, -2)))
    }
  }
}
