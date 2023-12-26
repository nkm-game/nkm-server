package com.tosware.nkm.models.game.hex

import com.softwaremill.quicklens.ModifyPimp
import com.tosware.nkm.*
import com.tosware.nkm.models.game.character.NkmCharacter
import com.tosware.nkm.models.game.event.RevealCondition
import com.tosware.nkm.models.game.game_state.{GameState, GameStatus}
import com.tosware.nkm.models.game.hex.HexCellType.Normal
import com.tosware.nkm.models.game.hex_effect.HexCellEffect

import scala.annotation.tailrec
import scala.util.Random

object HexCell {
  def empty(
      coordinates: HexCoordinates,
      cellType: HexCellType = Normal,
      spawnNumber: Option[Int] = None,
  ): HexCell = HexCell(coordinates, cellType, None, Seq.empty, spawnNumber)
}

case class HexCell(
    coordinates: HexCoordinates,
    cellType: HexCellType,
    characterId: Option[CharacterId],
    effects: Seq[HexCellEffect],
    spawnNumber: Option[Int],
) extends HexCellLike {
  val id: HexCoordinates = coordinates

  def addEffect(effect: HexCellEffect): HexCell =
    this.modify(_.effects).using(_ :+ effect)

  def removeEffect(effectId: HexCellEffectId): HexCell =
    this.modify(_.effects).using(_.filterNot(_.id == effectId))

  def getNeighbourOpt(direction: HexDirection)(implicit gameState: GameState): Option[HexCell] =
    coordinates.getNeighbour(direction).toCellOpt

  def getLine(
      direction: HexDirection,
      size: Int,
      stopPredicate: HexCell => Boolean = _ => false,
  )(implicit gameState: GameState): Seq[HexCell] = {
    if (size <= 0) return Seq.empty
    val neighbour = getNeighbourOpt(direction)
    if (neighbour.fold(true)(c => stopPredicate(c))) return Seq.empty
    neighbour.get +: neighbour.get.getLine(direction, size - 1)
  }

  def firstCharacterInLine(
      direction: HexDirection,
      size: Int,
      characterPredicate: NkmCharacter => Boolean = _ => true,
  )(implicit gameState: GameState): Option[NkmCharacter] = {
    @tailrec
    def scan(depth: Int, lastCell: HexCell): Option[NkmCharacter] = {
      if (depth == 0) return None
      lastCell.getNeighbourOpt(direction) match {
        case Some(neighbour) =>
          neighbour.characterOpt match {
            case Some(character) if characterPredicate(character) => Some(character)
            case _                                                => scan(depth - 1, neighbour)
          }
        case None => None
      }
    }
    scan(size, this)
  }

  def getLines(
      directions: Set[HexDirection],
      size: Int,
      stopPredicate: HexCell => Boolean = _ => false,
  )(implicit gameState: GameState): Set[HexCell] =
    directions.flatMap(d => getLine(d, size, stopPredicate))

  def getArea(
      depth: Int,
      searchFlags: Set[SearchFlag] = Set.empty,
      friendlyPlayerIdOpt: Option[PlayerId] = None,
      stopPredicate: HexCell => Boolean = _ => false,
  )(implicit gameState: GameState): Set[HexCell] = {
    implicit val hexMap: HexMap = gameState.hexMap

    def shouldStop(cell: HexCell): Boolean =
      searchFlags.contains(SearchFlag.StopAtWalls) && cell.cellType == HexCellType.Wall ||
        friendlyPlayerIdOpt.fold(false) { fpId =>
          searchFlags.contains(SearchFlag.StopAtEnemies) && cell.characterOpt.fold(false)(_.isEnemyFor(fpId)) ||
          searchFlags.contains(SearchFlag.StopAtFriends) && cell.characterOpt.fold(false)(_.isFriendFor(fpId))
        } ||
        stopPredicate(cell)

    @tailrec
    def getAreaInner(innerDepth: Int, fringes: Set[HexCell], visited: Set[HexCell]): Set[HexCell] = {
      if (innerDepth <= 0 || fringes.isEmpty) return visited
      val newFringes: Set[HexCell] =
        fringes.flatMap(_.getLines(HexDirection.values.toSet, 1, shouldStop)) -- fringes -- visited
      val newVisited: Set[HexCell] =
        visited ++ newFringes

      val fringesToRemove =
        friendlyPlayerIdOpt.fold(Set.empty[HexCell]) { fpId =>
          val ff = if (searchFlags.contains(SearchFlag.StopAfterFriends))
            newFringes.whereFriendsOf(fpId)
          else Set.empty
          val fe = if (searchFlags.contains(SearchFlag.StopAfterEnemies))
            newFringes.whereEnemiesOf(fpId)
          else Set.empty
          ff ++ fe
        }

      getAreaInner(innerDepth - 1, newFringes -- fringesToRemove, newVisited)
    }
    if (searchFlags.contains(SearchFlag.StraightLine))
      getLines(HexDirection.values.toSet, depth, shouldStop) + this
    else getAreaInner(depth, Set(this), Set(this))
  }

  def findClosestFreeCell(implicit gameState: GameState, random: Random): Option[HexCell] = {
    @tailrec
    def findClosestFreeCellInner(depth: Int, lastFoundCellsCount: Int): Option[HexCell] = {
      val foundCells = coordinates.getCircle(depth).flatMap(_.toCellOpt)
      if (foundCells.size == lastFoundCellsCount) None
      else {
        val emptyCells = foundCells.filter(_.isFreeToStand)
        if (emptyCells.isEmpty) findClosestFreeCellInner(depth + 1, foundCells.size)
        else Some(random.shuffle(emptyCells.toSeq).head)
      }
    }

    findClosestFreeCellInner(1, 0)
  }

  def toTemplate: HexCellTemplate =
    HexCellTemplate(coordinates, cellType, spawnNumber)

  def toView(forPlayerOpt: Option[PlayerId])(implicit gameState: GameState): HexCellView = {
    val characterIdOpt =
      if (gameState.invisibleCharacterCoords(forPlayerOpt).contains(coordinates))
        None
      else if (
        gameState.gameStatus == GameStatus.CharacterPlacing && characterOpt(gameState).fold(true)(c =>
          !forPlayerOpt.contains(c.owner.id)
        )
      )
        None
      else characterId

    val hiddenTrapIds =
      gameState
        .hiddenEvents
        .filterNot(he => forPlayerOpt.exists(forPlayerId => he.showOnlyFor.contains(forPlayerId)))
        .map(_.revealCondition)
        .ofType[RevealCondition.RelatedTrapRevealed]
        .map(_.effectId)

    val effectsFiltered =
      effects.filterNot(e => hiddenTrapIds.contains(e.id))

    HexCellView(
      coordinates = coordinates,
      cellType = cellType,
      characterId = characterIdOpt,
      effects = effectsFiltered.map(_.toView),
      spawnNumber = spawnNumber,
    )
  }
}
