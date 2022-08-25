package com.tosware.NKM.models.game.hex

import com.tosware.NKM.models.game.Ability.AbilityId
import com.tosware.NKM.models.game.GameEvent.{ContainsAbilityId, ContainsCharacterId, GameEvent}
import com.tosware.NKM.models.game.{GameState, NKMCharacter}
import com.tosware.NKM.models.game.NKMCharacter.CharacterId
import com.tosware.NKM.models.game.Player.PlayerId

import scala.math.abs
import scala.reflect.ClassTag
import scala.util.Random

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

    def whereFriendsOfC(characterId: CharacterId): Set[HexCoordinates] =
      toCells.whereFriendsOfC(characterId).toCoords

    def whereEnemiesOfC(characterId: CharacterId): Set[HexCoordinates] =
      toCells.whereEnemiesOfC(characterId).toCoords
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

    def whereFriendsOfC(characterId: CharacterId): Set[HexCell] =
      friendsOfC(characterId).map(_.parentCell.get)

    def whereEnemiesOfC(characterId: CharacterId): Set[HexCell] =
      enemiesOfC(characterId).map(_.parentCell.get)

    def friendsOfC(characterId: CharacterId): Set[NKMCharacter] =
      characters.filter(_.isFriendForC(characterId))

    def enemiesOfC(characterId: CharacterId): Set[NKMCharacter] =
      characters.filter(_.isEnemyForC(characterId))

    def whereFriendsOf(playerId: PlayerId): Set[HexCell] =
      friendsOf(playerId).map(_.parentCell.get)

    def whereEnemiesOf(playerId: PlayerId): Set[HexCell] =
      enemiesOf(playerId).map(_.parentCell.get)

    def friendsOf(playerId: PlayerId): Set[NKMCharacter] =
      characters.filter(_.isFriendFor(playerId))

    def enemiesOf(playerId: PlayerId): Set[NKMCharacter] =
      characters.filter(_.isEnemyFor(playerId))
  }
  implicit class GameEventSeqUtils[T <: GameEvent](es: Seq[T]) {
    def inPhase(number: Int): Seq[T] =
      es.filter(_.phase.number == number)
    def inTurn(number: Int): Seq[T] =
      es.filter(_.turn.number == number)
    def causedBy(id: String): Seq[T] =
      es.filter(_.causedById == id)
    def ofCharacter(id: CharacterId): Seq[T with ContainsCharacterId] =
      es.ofType[T with ContainsCharacterId].filter(_.characterId == id)
    def ofAbility(id: AbilityId): Seq[T with ContainsAbilityId] =
      es.ofType[T with ContainsAbilityId].filter(_.abilityId == id)
  }

  implicit class SeqUtils[T](es: Seq[T]) {
    def ofType[A: ClassTag]: Seq[A] =
      es.collect {case e: A => e}
  }
}


object NKMUtils {
  def randomUUID(implicit random: Random): String =
    java.util.UUID.nameUUIDFromBytes(random.nextBytes(16)).toString
}
