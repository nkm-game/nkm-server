package unit

import com.tosware.nkm.Logging
import com.tosware.nkm.{CoordinateSeq, CoordinateSet}
import com.tosware.nkm.models.game.hex.{HexCoordinates, HexDirection}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import scala.math._

class HexCoordinatesSpec
  extends AnyWordSpecLike
    with Matchers
    with Logging
{
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
      HexCoordinates(0, 0).getLine(HexDirection.E, 0) should be (Seq.empty)
      HexCoordinates(-2, 2).getLine(HexDirection.SE, 3) should be (CoordinateSeq((-1, 1), (0, 0), (1, -1)))
    }

    "calculate correct lines" in {
      HexCoordinates(0, 0).getLines(Set(), 2) should be (Set.empty)
      HexCoordinates(0, 0).getLines(Set(HexDirection.W, HexDirection.SW), 0) should be (Set.empty)
      HexCoordinates(0, 0).getLines(Set(HexDirection.W, HexDirection.SW), 1) should be (CoordinateSet((-1, 0), (0, -1)))
      HexCoordinates(0, 0).getLines(Set(HexDirection.W, HexDirection.SW), 2) should be (CoordinateSet((-1, 0), (0, -1), (-2, 0), (0, -2)))
    }

    "calculate correct circles" in {
      val testCoordinates = CoordinateSeq((0, 0), (-1, 1), (1, 1), (20, 23), (1, -1), (-10, -200))
      testCoordinates foreach { c =>
        c.getCircle(-1) should be (Set.empty)
        c.getCircle(0) should be (Set(c))
        val c1 = c.getCircle(1)
        val c2 = c.getCircle(2)
        val c3 = c.getCircle(3)

        c1.size should be (1 + 6)
        c2.size should be (1 + 6 + 12)
        c3.size should be (1 + 6 + 12 + 18)

        assert(c2.contains(HexCoordinates(c.x + 2, c.z)))

        c1 foreach { co =>
          abs(co.x - c.x) + abs(co.y - c.y) + abs(co.z - c.z) should be <= 2
        }

        c2 foreach { co =>
          abs(co.x - c.x) + abs(co.y - c.y) + abs(co.z - c.z) should be <= 4
        }

        c3 foreach { co =>
          abs(co.x - c.x) + abs(co.y - c.y) + abs(co.z - c.z) should be <= 6
        }
      }
    }

    "calculate correct coords in directions" in {
      HexCoordinates(0, 0).getInDirection(HexDirection.W, 0) should be (HexCoordinates(0, 0))
      HexCoordinates(-2, 2).getInDirection(HexDirection.NE, 1) should be (HexCoordinates(-2, 3))
      HexCoordinates(0, -2).getInDirection(HexDirection.NW, 1) should be (HexCoordinates(-1, -1))
      HexCoordinates(0, 0).getInDirection(HexDirection.SW, 2) should be (HexCoordinates(0, -2))
      HexCoordinates(0, 0).getInDirection(HexDirection.SE, 2) should be (HexCoordinates(2, -2))
      HexCoordinates(0, -3).getInDirection(HexDirection.E, 10) should be (HexCoordinates(10, -3))
    }

    "calculate correct directions from coords" in {
      HexCoordinates(0, 0).getDirection(HexCoordinates(0, 0)) should be (None)
      HexCoordinates(-2, 2).getDirection(HexCoordinates(-2, 3)) should be (Some(HexDirection.NE))
      HexCoordinates(0, -2).getDirection(HexCoordinates(-1, -1)) should be (Some(HexDirection.NW))
      HexCoordinates(0, 0).getDirection(HexCoordinates(0, -2)) should be (Some(HexDirection.SW))
      HexCoordinates(0, 0).getDirection(HexCoordinates(2, -2)) should be (Some(HexDirection.SE))
      HexCoordinates(0, -3).getDirection(HexCoordinates(10, -3)) should be (Some(HexDirection.E))
      HexCoordinates(0, -3).getDirection(HexCoordinates(1, 1)) should be (None)
    }
  }
}
