package com.tosware.NKM.models.game.hex

import com.tosware.NKM.models.game.GameEvent.GameEvent
import com.tosware.NKM.models.game.{GameState, NKMCharacter}
import com.tosware.NKM.models.game.NKMCharacter.CharacterId

import scala.math.abs
import scala.reflect.ClassTag

object HexUtils {

  // TODO in Scala 3
  // type HexParam = (Int, Int) | (Int, Int, HexCellType) | (Int, Int, HexCellType, Int)

  def hexCellParamsToCells(params: Set[Any]): Set[HexCell] = {
    params.map {
      case (x: Int, y: Int) => HexCell.empty(HexCoordinates(x, y))
      case (x: Int, y: Int, t: HexCellType) => HexCell.empty(HexCoordinates(x, y), t)
      case (x: Int, y: Int, t: HexCellType, i: Int) => HexCell.empty(HexCoordinates(x, y), t, Some(i))
    }
  }

  def getAdjacentCells(cells: Set[HexCell], targetCellCoordinates: HexCoordinates): Set[HexCell] = {
    cells.filter(c =>
      abs(c.coordinates.x - targetCellCoordinates.x) <= 1
        && abs(c.coordinates.y - targetCellCoordinates.y) <= 1
        && abs(c.coordinates.z - targetCellCoordinates.z) <= 1
    ).filter(c => c.coordinates != targetCellCoordinates)
  }

  def CoordinateSeq(tuples: (Int, Int)*): Seq[HexCoordinates] = tuples.map{case (x, z) => HexCoordinates(x, z)}
  def CoordinateSet(tuples: (Int, Int)*): Set[HexCoordinates] = CoordinateSeq(tuples: _*).toSet

  implicit class HexCoordinatesSetUtils(coords: Set[HexCoordinates])(implicit gameState: GameState) {
    def toCells: Set[HexCell] =
      gameState.hexMap.get.cells.filter(c => coords.contains(c.coordinates))

    def characters: Set[NKMCharacter] =
      toCells.characters

    def whereExists(implicit gameState: GameState): Set[HexCoordinates] =
      toCells.map(_.coordinates)

    def whereEmpty: Set[HexCoordinates] =
      toCells.whereEmpty.map(_.coordinates)

    def whereFriendsOf(characterId: CharacterId): Set[HexCoordinates] =
      toCells.friendsOf(characterId).map(_.parentCell.get.coordinates)

    def whereEnemiesOf(characterId: CharacterId): Set[HexCoordinates] =
      toCells.enemiesOf(characterId).map(_.parentCell.get.coordinates)
  }

  implicit class HexCoordinatesSeqUtils(coords: Seq[HexCoordinates])(implicit gameState: GameState) {
    def toCells: Seq[HexCell] =
      coords.flatMap(c => gameState.hexMap.get.getCell(c))
  }

  implicit class CharacterIdSetUtils(ids: Set[CharacterId])(implicit gameState: GameState) {
    def toCharacters: Set[NKMCharacter] =
      ids.flatMap(id => gameState.characters.find(_.id == id))
  }

  implicit class HexCellSetUtils(cells: Set[HexCell])(implicit gameState: GameState) {
    def toCoords: Set[HexCoordinates] =
      cells.map(_.coordinates)

    def characterIds: Set[CharacterId] =
      cells.flatMap(_.characterId)

    def characters: Set[NKMCharacter] =
      characterIds.toCharacters

    def whereEmpty: Set[HexCell] =
      cells.filter(_.isEmpty)

    def friendsOf(characterId: CharacterId): Set[NKMCharacter] =
      characters.filter(_.isFriendFor(characterId))

    def enemiesOf(characterId: CharacterId): Set[NKMCharacter] =
      characters.filter(_.isEnemyFor(characterId))
  }
  implicit class GameEventSeqUtils(es: Seq[GameEvent]) {
    def ofType[T <: GameEvent: ClassTag]: Seq[T] = es.collect {case e: T => e}
  }
}

