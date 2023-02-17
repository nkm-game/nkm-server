package com.tosware.nkm.models.game.hex

import com.softwaremill.quicklens._
import com.tosware.nkm.models.game.Player.PlayerId
import com.tosware.nkm.models.game.effects.Invisibility
import com.tosware.nkm.models.game.{GameState, GameStatus}

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
      case cell if gameState.gameStatus == GameStatus.CharacterPlacing &&
        cell.characterOpt(gameState).fold(true)(c => !forPlayerOpt.contains(c.owner.id))
      => cell.modify(_.characterId).setTo(None)
      case cell => cell
    }

    HexMapView(name, hiddenCharactersMap.cells)
  }
}
