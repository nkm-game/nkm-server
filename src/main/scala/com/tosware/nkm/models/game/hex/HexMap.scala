package com.tosware.nkm.models.game.hex

import com.tosware.nkm.models.game.GameState
import com.tosware.nkm.models.game.NkmCharacter.CharacterId
import com.tosware.nkm.models.game.Player.PlayerId
import com.tosware.nkm.models.game.effects.Invisibility
import com.softwaremill.quicklens._

trait HexMapLike {
  val name: String
  val cells: Set[HexCell]

  def getCell(hexCoordinates: HexCoordinates): Option[HexCell] = cells.find(_.coordinates == hexCoordinates)

  def getSpawnPoints: Set[HexCell] = cells.filter(c => c.cellType == HexCellType.SpawnPoint)

  def getSpawnPointsByNumber(n: Int): Set[HexCell] = getSpawnPoints.filter(_.spawnNumber.forall(_ == n))

  def getSpawnPointsFor(playerId: PlayerId)(implicit gameState: GameState): Set[HexCell] =
    getSpawnPointsByNumber(gameState.playerNumber(playerId))

  def maxNumberOfCharacters: Int = getSpawnPoints.map(_.spawnNumber.get).size

  def getCellOfCharacter(id: CharacterId): Option[HexCell] =
    cells.find(c => c.characterId.nonEmpty && c.characterId.get == id)

  def toTextUi: String = {
    val coords = cells.map(_.coordinates)
    val minX = coords.map(_.x).min
    val maxX = coords.map(_.x).max
    val minZ = coords.map(_.z).min
    val maxZ = coords.map(_.z).max

    var canvas = ""
    var offsetSize = 0

    val nullCell = "  "

    for(z <- minZ to maxZ) {
      canvas += "\n"
      canvas += nullCell * offsetSize
      offsetSize += 1

      for(x <- minX to maxX) {
        val cellString = cells.find(_.coordinates == HexCoordinates(x, z)) match {
          case Some(cell) =>
            cell.cellType match {
              case HexCellType.Transparent => nullCell
              case HexCellType.Normal => "░░"
              case HexCellType.Wall => "██"
              case HexCellType.SpawnPoint => "S" + cell.spawnNumber.get
            }
          case None => nullCell
        }
        canvas += nullCell + cellString
      }
    }

    // reverse the lines as Z coordinate is going in descending order and trim whitespace at end lines
    canvas = canvas.linesIterator.toSeq.reverse.mkString("\n").replaceAll("""(?m)\s+$""", "")
    "\n" + canvas + "\n"
  }

  override def toString: String = name
}

object HexMap {
  def empty: HexMap = HexMap("Empty HexMap", Set.empty)
}

case class HexMap(name: String, cells: Set[HexCell]) extends HexMapLike {
  def toView(forPlayerOpt: Option[PlayerId])(implicit gameState: GameState): HexMapView = {
    val otherInvisibleCharacterCoords =
      gameState.characters
        .filterNot(c => forPlayerOpt.contains(c.owner.id))
        .filter(_.state.effects.ofType[Invisibility].nonEmpty)
        .flatMap(_.parentCell.map(_.coordinates))

    val hiddenCharactersMap = this.modify(_.cells.each).using {
      case cell if otherInvisibleCharacterCoords.contains(cell.coordinates) => cell.modify(_.characterId).setTo(None)
      case cell => cell
    }

    HexMapView(name, hiddenCharactersMap.cells)
  }
}

case class HexMapView(name: String, cells: Set[HexCell]) extends HexMapLike
