package unit

import com.tosware.NKM.HexMapProvider
import com.tosware.NKM.models.game.HexCellType._
import com.tosware.NKM.models.game.{HexMap, HexUtils}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.slf4j.{Logger, LoggerFactory}

class HexMapSpec
  extends AnyWordSpecLike
    with Matchers {
  val logger: Logger = LoggerFactory.getLogger(getClass)
  "HexMap" must {
    "display text Ui in test map" in {
      val hexParams: Set[Any] = Set(
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
      val cells = HexUtils.hexCellParamsToCells(hexParams)

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
      logger.info(hexMaps.map(_.toTextUi).toString())
    }
  }
}
