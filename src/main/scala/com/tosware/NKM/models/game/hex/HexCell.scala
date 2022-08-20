package com.tosware.NKM.models.game.hex

import com.tosware.NKM.models.game.NKMCharacter.CharacterId
import com.tosware.NKM.models.game.Player.PlayerId
import com.tosware.NKM.models.game.hex.HexCellType.Normal
import com.tosware.NKM.models.game.{GameState, NKMCharacter}
import HexUtils._

import scala.annotation.tailrec

case class HexCellEffect(cooldown: Int)

object HexCell {
  def empty
  (
    coordinates: HexCoordinates,
    cellType: HexCellType = Normal,
    spawnNumber: Option[Int] = None,
  ): HexCell = HexCell(coordinates, cellType, None, Seq.empty, spawnNumber)
}

case class HexCell
(
  coordinates: HexCoordinates,
  cellType: HexCellType,
  characterId: Option[CharacterId],
  effects: Seq[HexCellEffect],
  spawnNumber: Option[Int],
) {
  def isEmpty: Boolean = characterId.isEmpty
  def isFreeToStand: Boolean = isEmpty && cellType != HexCellType.Wall
  def isFreeToMove(forCharacterId: CharacterId)(implicit gameState: GameState): Boolean =
    cellType != HexCellType.Wall && characterId.flatMap(cid => gameState.characterById(cid)).fold(true)(_.isFriendForC(forCharacterId))
  def character(implicit gameState: GameState): Option[NKMCharacter] =
    characterId.flatMap(cid => gameState.characterById(cid))

  def getNeighbour(direction: HexDirection)(implicit hexMap: HexMap): Option[HexCell] =
    coordinates.getNeighbour(direction).toCellOpt

  def getLine(
               direction: HexDirection,
               size: Int,
               stopPredicate: HexCell => Boolean = _ => false,
             )(implicit hexMap: HexMap): Set[HexCell] = {
    if(size <= 0) return Set.empty
    val neighbour = getNeighbour(direction)
    if(neighbour.fold(true)(c => stopPredicate(c))) return Set.empty
    neighbour.get.getLine(direction, size - 1) + neighbour.get
  }

  def firstCharacterInLine
  (
    direction: HexDirection,
    size: Int,
  )(implicit gameState: GameState): Option[NKMCharacter] = {
    implicit val hexMap: HexMap = gameState.hexMap.get

    @tailrec
    def scan(depth: Int, lastCell: HexCell): Option[NKMCharacter] = {
      if(depth == 0) return None
      val neighbour = lastCell.getNeighbour(direction)
      if(neighbour.isEmpty) return None
      if(neighbour.get.characterId.isDefined) {
        return Some(neighbour.get.character.get)
      }
      scan(depth - 1, neighbour.get)
    }
    scan(size, this)
  }

  def getLines(
               directions: Set[HexDirection],
               size: Int,
               stopPredicate: HexCell => Boolean = _ => false,
             )(implicit hexMap: HexMap): Set[HexCell] =
    directions.flatMap(d => getLine(d, size, stopPredicate))

  def getArea(
               depth: Int,
               searchFlags: Set[SearchFlag] = Set.empty,
               friendlyPlayerIdOpt: Option[PlayerId] = None,
               stopPredicate: HexCell => Boolean = _ => false,
             )(implicit gameState: GameState): Set[HexCell] = {
    implicit val hexMap: HexMap = gameState.hexMap.get

    def shouldStop(cell: HexCell): Boolean = {
      searchFlags.contains(SearchFlag.StopAtWalls) && cell.cellType == HexCellType.Wall ||
      friendlyPlayerIdOpt.fold(false)(fpId => {
        searchFlags.contains(SearchFlag.StopAtEnemies) && cell.character.fold(false)(_.isEnemyFor(fpId)) ||
          searchFlags.contains(SearchFlag.StopAtFriends) && cell.character.fold(false)(_.isFriendFor(fpId))
      }) ||
        stopPredicate(cell)
    }

    @tailrec
    def getAreaInner(innerDepth: Int, fringes: Set[HexCell], visited: Set[HexCell]): Set[HexCell] = {
      if(innerDepth <= 0 || fringes.isEmpty) return visited
      val newFringes: Set[HexCell] =
        fringes.flatMap(_.getLines(HexDirection.values.toSet, 1, shouldStop)) -- fringes -- visited
      val newVisited: Set[HexCell] =
        visited ++ newFringes

      val fringesToRemove =
        friendlyPlayerIdOpt.fold(Set.empty[HexCell]){ fpId =>
          val ff = if(searchFlags.contains(SearchFlag.StopAfterFriends))
            newFringes.whereFriendsOf(fpId)
          else Set.empty
          val fe = if(searchFlags.contains(SearchFlag.StopAfterEnemies))
            newFringes.whereEnemiesOf(fpId)
          else Set.empty
          ff ++ fe
        }

      getAreaInner(innerDepth - 1, newFringes -- fringesToRemove, newVisited)
    }
    if(searchFlags.contains(SearchFlag.StraightLine))
      getLines(HexDirection.values.toSet, depth, shouldStop) + this
    else getAreaInner(depth, Set(this), Set(this))
  }
}
