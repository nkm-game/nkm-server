package unit

import com.tosware.NKM.HexMapProvider
import com.tosware.NKM.models.game.HexCellType._
import com.tosware.NKM.models.game.{HexCell, HexCellType, HexCoordinates, HexMap}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class HexMapSpec
  extends AnyWordSpecLike
    with Matchers {
  "HexMap" must {
    "display text Ui in test map" in {
      val cells: Set[HexCell] = {
        Set(
          (-1, 1, Wall),
          (0, 1, Wall),
          (1, 1, Wall),
          (2, 1, Wall),
          (3, 1, Wall),
          (0, 0, SpawnPoint, 1),
          (1, 0),
          (2, 0),
          (3, 0, SpawnPoint, 2),
          (0, -1, Wall),
          (1, -1, Wall),
          (2, -1, Wall),
          (3, -1, Wall),
          (4, -1, Wall),
        )
        .map{
          case (x: Int, y: Int) => HexCell.empty(HexCoordinates(x, y))
          case (x: Int, y: Int, t: HexCellType) => HexCell.empty(HexCoordinates(x, y), t)
          case (x: Int, y: Int, t: HexCellType, i: Int) => HexCell.empty(HexCoordinates(x, y), t, Some(i))
        }
      }

      val hexMap = HexMap("test", cells)
      val expected =
        """
          |      ██  ██  ██  ██  ██
          |        S1  ░░  ░░  S2
          |      ██  ██  ██  ██  ██
          |
          |""".stripMargin

      hexMap.toTextUi.filterNot(_.isWhitespace) shouldBe expected.filterNot(_.isWhitespace)
    }

    // use for visualisation
    "display text Ui in other maps" in {
      val hexMaps = HexMapProvider().getHexMaps
      println(hexMaps.map(_.toTextUi))
    }
  }
}
