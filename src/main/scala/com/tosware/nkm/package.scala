package com.tosware

import com.tosware.nkm.models.game.character.NkmCharacter
import com.tosware.nkm.models.game.event.GameEvent.*
import com.tosware.nkm.models.game.game_state.GameState
import com.tosware.nkm.models.game.hex.*
import org.slf4j.LoggerFactory

import scala.math.abs
import scala.reflect.ClassTag
import scala.util.Random

package object nkm {
  private val log = LoggerFactory.getLogger(this.getClass)
  type UserId = String
  type PlayerId = String
  type GameId = String

  type GameEventId = String

  type CharacterMetadataId = String
  type CharacterId = String

  type AbilityMetadataId = String
  type AbilityId = String

  /** if the check on the left is false, then the check fails with message on right
    */
  type UseCheck = (Boolean, String)

  type CharacterEffectMetadataId = String
  type CharacterEffectId = String

  type HexCellEffectMetadataId = String
  type HexCellEffectId = String

  type HexPointGroupId = String

  type BugReportId = String
  def randomUUID()(implicit random: Random): String = {
    val uuid = java.util.UUID.nameUUIDFromBytes(random.nextBytes(16)).toString
    log.debug(s"RANDOM UUID GENERATED: $uuid")
    uuid
  }

  def hexCellParamsToCells(params: Set[Any]): Set[HexCell] =
    params.map {
      case (x: Int, y: Int)                         => HexCell.empty(HexCoordinates(x, y))
      case (x: Int, y: Int, t: HexCellType)         => HexCell.empty(HexCoordinates(x, y), t)
      case (x: Int, y: Int, t: HexCellType, i: Int) => HexCell.empty(HexCoordinates(x, y), t, Some(i))
    }

  def pointParamsToPointGroups(params: Seq[(Set[(Int, Int)], Int)]): Seq[HexPointGroup] =
    params.map { x =>
      val pointsSet = x._1
      val pointsPerPhase = x._2
      val coordsSet = pointsSet.map(coordTuple => HexCoordinates(coordTuple))
      HexPointGroup(coordsSet, pointsPerPhase)
    }

  def getAdjacentCells[T <: HexCellLike](cells: Set[T], targetCellCoordinates: HexCoordinates): Set[T] =
    cells.filter(c =>
      abs(c.coordinates.x - targetCellCoordinates.x) <= 1
        && abs(c.coordinates.y - targetCellCoordinates.y) <= 1
        && abs(c.coordinates.z - targetCellCoordinates.z) <= 1
    ).filter(c => c.coordinates != targetCellCoordinates)

  def CoordinateSeq(tuples: (Int, Int)*): Seq[HexCoordinates] = tuples.map { case (x, z) => HexCoordinates(x, z) }
  def CoordinateSet(tuples: (Int, Int)*): Set[HexCoordinates] = CoordinateSeq(tuples*).toSet

  implicit class HexCoordinatesSetUtils(coords: Set[HexCoordinates])(implicit gameState: GameState) {
    def toCells: Set[HexCell] =
      gameState.hexMap.cells.filter(c => coords.contains(c.coordinates))

    def characters: Set[NkmCharacter] =
      toCells.characters

    def whereCharacters: Set[HexCoordinates] =
      toCells.whereCharacters.toCoords

    def whereExists: Set[HexCoordinates] =
      toCells.map(_.coordinates)

    def whereEmpty: Set[HexCoordinates] =
      toCells.whereEmpty.map(_.coordinates)

    def whereFreeToStand: Set[HexCoordinates] =
      toCells.whereFreeToStand.toCoords

    def whereFreeToPass(forCharacterId: CharacterId): Set[HexCoordinates] =
      toCells.whereFreeToPass(forCharacterId).toCoords

    def whereFriendsOfC(characterId: CharacterId): Set[HexCoordinates] =
      toCells.whereFriendsOfC(characterId).toCoords

    def whereEnemiesOfC(characterId: CharacterId): Set[HexCoordinates] =
      toCells.whereEnemiesOfC(characterId).toCoords

    def whereSeenEnemiesOfC(characterId: CharacterId): Set[HexCoordinates] =
      toCells.whereSeenEnemiesOfC(characterId).toCoords
  }

  implicit class HexCoordinatesSeqUtils(coords: Seq[HexCoordinates])(implicit gameState: GameState) {
    def toCells: Seq[HexCell] =
      coords.flatMap(c => gameState.hexMap.getCellOpt(c))

    def characters: Seq[NkmCharacter] =
      toCells.characters

    def whereCharacters: Seq[HexCoordinates] =
      toCells.whereCharacters.toCoords

    def whereExists: Seq[HexCoordinates] =
      toCells.map(_.coordinates)

    def whereEmpty: Seq[HexCoordinates] =
      toCells.whereEmpty.map(_.coordinates)

    def whereFreeToStand: Seq[HexCoordinates] =
      toCells.whereFreeToStand.toCoords

    def whereFreeToPass(forCharacterId: CharacterId): Seq[HexCoordinates] =
      toCells.whereFreeToPass(forCharacterId).toCoords

    def whereFriendsOfC(characterId: CharacterId): Seq[HexCoordinates] =
      toCells.whereFriendsOfC(characterId).toCoords

    def whereEnemiesOfC(characterId: CharacterId): Seq[HexCoordinates] =
      toCells.whereEnemiesOfC(characterId).toCoords
  }

  implicit class CharacterIdSetUtils(ids: Set[CharacterId])(implicit gameState: GameState) {
    def toCharacters: Set[NkmCharacter] =
      ids.flatMap(id => gameState.characters.find(_.id == id))
  }

  implicit class CharacterIdSeqUtils(ids: Seq[CharacterId])(implicit gameState: GameState) {
    def toCharacters: Seq[NkmCharacter] =
      ids.flatMap(id => gameState.characters.find(_.id == id))
  }

  implicit class HexCellSetUtils(cells: Set[HexCell])(implicit gameState: GameState) {
    def toCoords: Set[HexCoordinates] =
      cells.map(_.coordinates)

    def characterIds: Set[CharacterId] =
      cells.flatMap(_.characterId)

    def characters: Set[NkmCharacter] =
      characterIds.toCharacters

    def whereCharacters: Set[HexCell] =
      cells.filter(_.characterId.isDefined)

    def whereEmpty: Set[HexCell] =
      cells.filter(_.isEmpty)

    def whereFreeToStand: Set[HexCell] =
      cells.filter(_.isFreeToStand)

    def whereFreeToPass(forCharacterId: CharacterId): Set[HexCell] =
      cells.filter(_.isFreeToPass(forCharacterId))

    def whereFriendsOfC(characterId: CharacterId): Set[HexCell] =
      friendsOfC(characterId).flatMap(_.parentCellOpt)

    def whereEnemiesOfC(characterId: CharacterId): Set[HexCell] =
      enemiesOfC(characterId).flatMap(_.parentCellOpt)

    def whereSeenEnemiesOfC(characterId: CharacterId): Set[HexCell] =
      enemiesOfC(characterId).filterNot(_.isInvisible).flatMap(_.parentCellOpt)

    def friendsOfC(characterId: CharacterId): Set[NkmCharacter] =
      characters.filter(_.isFriendForC(characterId))

    def enemiesOfC(characterId: CharacterId): Set[NkmCharacter] =
      characters.filter(_.isEnemyForC(characterId))

    def whereFriendsOf(playerId: PlayerId): Set[HexCell] =
      friendsOf(playerId).flatMap(_.parentCellOpt)

    def whereEnemiesOf(playerId: PlayerId): Set[HexCell] =
      enemiesOf(playerId).flatMap(_.parentCellOpt)

    def friendsOf(playerId: PlayerId): Set[NkmCharacter] =
      characters.filter(_.isFriendFor(playerId))

    def enemiesOf(playerId: PlayerId): Set[NkmCharacter] =
      characters.filter(_.isEnemyFor(playerId))
  }

  implicit class HexCellSeqUtils(cells: Seq[HexCell])(implicit gameState: GameState) {
    def toCoords: Seq[HexCoordinates] =
      cells.map(_.coordinates)

    def characters: Seq[NkmCharacter] =
      characterIds.toCharacters

    def characterIds: Seq[CharacterId] =
      cells.flatMap(_.characterId)

    def whereCharacters: Seq[HexCell] =
      cells.filter(_.characterId.isDefined)

    def whereEmpty: Seq[HexCell] =
      cells.filter(_.isEmpty)

    def whereFreeToStand: Seq[HexCell] =
      cells.filter(_.isFreeToStand)

    def whereFreeToPass(forCharacterId: CharacterId): Seq[HexCell] =
      cells.filter(_.isFreeToPass(forCharacterId))

    def whereFriendsOfC(characterId: CharacterId): Seq[HexCell] =
      friendsOfC(characterId).flatMap(_.parentCellOpt)

    def whereEnemiesOfC(characterId: CharacterId): Seq[HexCell] =
      enemiesOfC(characterId).flatMap(_.parentCellOpt)

    def friendsOfC(characterId: CharacterId): Seq[NkmCharacter] =
      characters.filter(_.isFriendForC(characterId))

    def enemiesOfC(characterId: CharacterId): Seq[NkmCharacter] =
      characters.filter(_.isEnemyForC(characterId))

    def whereFriendsOf(playerId: PlayerId): Seq[HexCell] =
      friendsOf(playerId).flatMap(_.parentCellOpt)

    def whereEnemiesOf(playerId: PlayerId): Seq[HexCell] =
      enemiesOf(playerId).flatMap(_.parentCellOpt)

    def friendsOf(playerId: PlayerId): Seq[NkmCharacter] =
      characters.filter(_.isFriendFor(playerId))

    def enemiesOf(playerId: PlayerId): Seq[NkmCharacter] =
      characters.filter(_.isEnemyFor(playerId))
  }

  implicit class HexDirectionUtils(direction: HexDirection) {
    def lookRight: HexDirection =
      direction match {
        case HexDirection.NE =>
          HexDirection.E
        case HexDirection.E =>
          HexDirection.SE
        case HexDirection.SE =>
          HexDirection.SW
        case HexDirection.SW =>
          HexDirection.W
        case HexDirection.W =>
          HexDirection.NW
        case HexDirection.NW =>
          HexDirection.NE
      }
    def lookLeft: HexDirection =
      direction match {
        case HexDirection.NE =>
          HexDirection.NW
        case HexDirection.E =>
          HexDirection.NE
        case HexDirection.SE =>
          HexDirection.E
        case HexDirection.SW =>
          HexDirection.SE
        case HexDirection.W =>
          HexDirection.SW
        case HexDirection.NW =>
          HexDirection.W
      }
  }

  implicit class GameEventSeqUtils[T <: GameEvent](es: Seq[T]) {
    def inPhase(number: Int): Seq[T] =
      es.filter(_.context.phase.number == number)
    def inTurn(number: Int): Seq[T] =
      es.filter(_.context.turn.number == number)
    def causedBy(id: String): Seq[T] =
      es.filter(_.context.causedById == id)
    def ofCharacter(id: CharacterId): Seq[T & ContainsCharacterId] =
      es.ofType[T & ContainsCharacterId].filter(_.characterId == id)
    def ofAbility(id: AbilityId): Seq[T & ContainsAbilityId] =
      es.ofType[T & ContainsAbilityId].filter(_.abilityId == id)
  }

  implicit class SeqUtils[T](es: Seq[T]) {
    def ofType[A: ClassTag]: Seq[A] =
      es.collect { case e: A => e }
  }
}
