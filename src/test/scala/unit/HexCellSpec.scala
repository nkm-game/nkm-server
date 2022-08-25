package unit

import com.tosware.nkm.Logging
import com.tosware.nkm.models.game.hex.HexUtils._
import com.tosware.nkm.models.game.hex._
import com.tosware.nkm.models.game.{CharacterMetadata, GameState}
import com.tosware.nkm.providers.HexMapProvider.TestHexMapName
import helpers.TestUtils
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class HexCellSpec
  extends AnyWordSpecLike
    with Matchers
    with Logging
    with TestUtils
{
  private val metadata = CharacterMetadata.empty()
  implicit val gameState: GameState = getTestGameState(TestHexMapName.Simple2v2, Seq(
    Seq(metadata.copy(name = "Empty1"), metadata.copy(name = "Empty2")),
    Seq(metadata.copy(name = "Empty3"), metadata.copy(name = "Empty4")),
  ))
  implicit val hexMap: HexMap = gameState.hexMap.get

  val p0FirstCharacterSpawnCoordinates = HexCoordinates(0, 0)
  val p0SecondCharacterSpawnCoordinates = HexCoordinates(-1, 0)
  val p1FirstCharacterSpawnCoordinates = HexCoordinates(3, 0)
  val p1SecondCharacterSpawnCoordinates = HexCoordinates(4, 0)

  val p0FirstCharacter = characterOnPoint(p0FirstCharacterSpawnCoordinates)
  val p0SecondCharacter = characterOnPoint(p0SecondCharacterSpawnCoordinates)

  val p1FirstCharacter = characterOnPoint(p1FirstCharacterSpawnCoordinates)
  val p1SecondCharacter = characterOnPoint(p1SecondCharacterSpawnCoordinates)
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
        Some(p0FirstCharacter.owner.id),
      ).toCoords should be (
        hexMap.cells.toCoords -- hexMap.cells.whereEnemiesOfC(p0FirstCharacter.id).toCoords
      )
    }
  }
}
